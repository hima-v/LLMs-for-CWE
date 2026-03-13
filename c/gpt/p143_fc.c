/* C (libmicrohttpd + SQLite) — GET /unsubscribe with one-time signed token, prepared statements, no enumeration
   Build (example): gcc -O2 -Wall unsubscribe.c -o unsubscribe -lmicrohttpd -lsqlite3

   Token format: base64url(raw).base64url(sig)
   sig = HMAC_SHA256(secret, raw)

   NOTE: mailing list call is stubbed. Replace with your HTTPS client.
*/
#include <microhttpd.h>
#include <sqlite3.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

/* ---------- Minimal SHA-256 + HMAC-SHA256 (compact, self-contained) ---------- */
/* Public domain style minimal implementation (not optimized). */

typedef struct { uint32_t s[8]; uint64_t bits; uint8_t buf[64]; size_t len; } sha256_ctx;
static uint32_t rotr32(uint32_t x, uint32_t n){ return (x>>n)|(x<<(32-n)); }
static uint32_t be32(const uint8_t *p){ return ((uint32_t)p[0]<<24)|((uint32_t)p[1]<<16)|((uint32_t)p[2]<<8)|p[3]; }
static void st32(uint8_t *p, uint32_t x){ p[0]=x>>24; p[1]=x>>16; p[2]=x>>8; p[3]=x; }

static const uint32_t K[64]={
  0x428a2f98,0x71374491,0xb5c0fbcf,0xe9b5dba5,0x3956c25b,0x59f111f1,0x923f82a4,0xab1c5ed5,
  0xd807aa98,0x12835b01,0x243185be,0x550c7dc3,0x72be5d74,0x80deb1fe,0x9bdc06a7,0xc19bf174,
  0xe49b69c1,0xefbe4786,0x0fc19dc6,0x240ca1cc,0x2de92c6f,0x4a7484aa,0x5cb0a9dc,0x76f988da,
  0x983e5152,0xa831c66d,0xb00327c8,0xbf597fc7,0xc6e00bf3,0xd5a79147,0x06ca6351,0x14292967,
  0x27b70a85,0x2e1b2138,0x4d2c6dfc,0x53380d13,0x650a7354,0x766a0abb,0x81c2c92e,0x92722c85,
  0xa2bfe8a1,0xa81a664b,0xc24b8b70,0xc76c51a3,0xd192e819,0xd6990624,0xf40e3585,0x106aa070,
  0x19a4c116,0x1e376c08,0x2748774c,0x34b0bcb5,0x391c0cb3,0x4ed8aa4a,0x5b9cca4f,0x682e6ff3,
  0x748f82ee,0x78a5636f,0x84c87814,0x8cc70208,0x90befffa,0xa4506ceb,0xbef9a3f7,0xc67178f2
};

static void sha256_init(sha256_ctx *c){
  c->s[0]=0x6a09e667; c->s[1]=0xbb67ae85; c->s[2]=0x3c6ef372; c->s[3]=0xa54ff53a;
  c->s[4]=0x510e527f; c->s[5]=0x9b05688c; c->s[6]=0x1f83d9ab; c->s[7]=0x5be0cd19;
  c->bits=0; c->len=0;
}

static void sha256_block(sha256_ctx *c, const uint8_t b[64]){
  uint32_t w[64];
  for(int i=0;i<16;i++) w[i]=be32(b+4*i);
  for(int i=16;i<64;i++){
    uint32_t s0=rotr32(w[i-15],7)^rotr32(w[i-15],18)^(w[i-15]>>3);
    uint32_t s1=rotr32(w[i-2],17)^rotr32(w[i-2],19)^(w[i-2]>>10);
    w[i]=w[i-16]+s0+w[i-7]+s1;
  }
  uint32_t a=c->s[0],b0=c->s[1],c0=c->s[2],d=c->s[3],e=c->s[4],f=c->s[5],g=c->s[6],h=c->s[7];
  for(int i=0;i<64;i++){
    uint32_t S1=rotr32(e,6)^rotr32(e,11)^rotr32(e,25);
    uint32_t ch=(e&f)^((~e)&g);
    uint32_t temp1=h+S1+ch+K[i]+w[i];
    uint32_t S0=rotr32(a,2)^rotr32(a,13)^rotr32(a,22);
    uint32_t maj=(a&b0)^(a&c0)^(b0&c0);
    uint32_t temp2=S0+maj;
    h=g; g=f; f=e; e=d+temp1; d=c0; c0=b0; b0=a; a=temp1+temp2;
  }
  c->s[0]+=a; c->s[1]+=b0; c->s[2]+=c0; c->s[3]+=d; c->s[4]+=e; c->s[5]+=f; c->s[6]+=g; c->s[7]+=h;
}

static void sha256_update(sha256_ctx *c, const void *data, size_t n){
  const uint8_t *p=(const uint8_t*)data;
  c->bits += (uint64_t)n*8;
  while(n){
    size_t take = 64 - c->len;
    if(take>n) take=n;
    memcpy(c->buf + c->len, p, take);
    c->len += take; p += take; n -= take;
    if(c->len==64){ sha256_block(c,c->buf); c->len=0; }
  }
}

static void sha256_final(sha256_ctx *c, uint8_t out[32]){
  c->buf[c->len++] = 0x80;
  if(c->len>56){
    while(c->len<64) c->buf[c->len++]=0;
    sha256_block(c,c->buf);
    c->len=0;
  }
  while(c->len<56) c->buf[c->len++]=0;
  for(int i=0;i<8;i++) c->buf[56+i]=(uint8_t)(c->bits>>(56-8*i));
  sha256_block(c,c->buf);
  for(int i=0;i<8;i++) st32(out+4*i, c->s[i]);
}

static void hmac_sha256(const uint8_t *key, size_t klen, const uint8_t *msg, size_t mlen, uint8_t out[32]){
  uint8_t kopad[64], kipad[64], kh[32];
  if(klen>64){
    sha256_ctx t; sha256_init(&t); sha256_update(&t,key,klen); sha256_final(&t,kh);
    key=kh; klen=32;
  }
  memset(kopad,0,64); memset(kipad,0,64);
  memcpy(kopad,key,klen); memcpy(kipad,key,klen);
  for(int i=0;i<64;i++){ kopad[i]^=0x5c; kipad[i]^=0x36; }
  uint8_t inner[32];
  sha256_ctx c; sha256_init(&c); sha256_update(&c,kipad,64); sha256_update(&c,msg,mlen); sha256_final(&c,inner);
  sha256_init(&c); sha256_update(&c,kopad,64); sha256_update(&c,inner,32); sha256_final(&c,out);
}

/* ---------------- Base64url decode (no padding) ---------------- */
static int b64url_val(char c){
  if(c>='A'&&c<='Z') return c-'A';
  if(c>='a'&&c<='z') return c-'a'+26;
  if(c>='0'&&c<='9') return c-'0'+52;
  if(c=='-') return 62;
  if(c=='_') return 63;
  return -1;
}

static int b64url_decode(const char *in, uint8_t **out, size_t *outlen){
  size_t n=strlen(in);
  size_t cap=(n*3)/4 + 4;
  uint8_t *buf=(uint8_t*)malloc(cap);
  if(!buf) return 0;
  size_t o=0;
  int val=0, valb=-8;
  for(size_t i=0;i<n;i++){
    int v=b64url_val(in[i]);
    if(v<0){ free(buf); return 0; }
    val=(val<<6)+v; valb+=6;
    if(valb>=0){
      buf[o++]=(uint8_t)((val>>valb)&0xFF);
      valb-=8;
    }
  }
  *out=buf; *outlen=o;
  return 1;
}

/* ---------------- App logic ---------------- */
static sqlite3 *g_db = NULL;
static uint8_t g_secret[64];
static size_t g_secret_len = 0;

static const char *GENERIC_JSON = "{\"message\":\"If this link is valid, you have been unsubscribed.\"}\n";

static void normalize_email(char *s){
  if(!s) return;
  // trim + lowercase ASCII
  size_t len=strlen(s);
  while(len && (s[len-1]==' '||s[len-1]=='\n'||s[len-1]=='\r'||s[len-1]=='\t')) s[--len]=0;
  size_t start=0;
  while(s[start]==' '||s[start]=='\n'||s[start]=='\r'||s[start]=='\t') start++;
  if(start) memmove(s, s+start, len-start+1);
  for(size_t i=0;s[i];i++){
    if(s[i]>='A'&&s[i]<='Z') s[i]=(char)(s[i]-'A'+'a');
  }
}

static int timing_safe_eq(const uint8_t *a, const uint8_t *b, size_t n){
  uint8_t r=0;
  for(size_t i=0;i<n;i++) r |= (uint8_t)(a[i]^b[i]);
  return r==0;
}

static char *verify_token_get_raw(const char *signed_token){
  if(!signed_token) return NULL;
  const char *dot=strchr(signed_token,'.');
  if(!dot) return NULL;
  size_t raw_len = (size_t)(dot - signed_token);
  size_t sig_len = strlen(dot+1);
  if(raw_len<10 || raw_len>2000 || sig_len<10 || sig_len>2000) return NULL;

  char *raw_b64 = (char*)malloc(raw_len+1);
  if(!raw_b64) return NULL;
  memcpy(raw_b64, signed_token, raw_len);
  raw_b64[raw_len]=0;

  uint8_t *raw=NULL, *sig=NULL;
  size_t raw_bytes=0, sig_bytes=0;

  if(!b64url_decode(raw_b64,&raw,&raw_bytes)){ free(raw_b64); return NULL; }
  if(!b64url_decode(dot+1,&sig,&sig_bytes)){ free(raw_b64); free(raw); return NULL; }
  free(raw_b64);

  uint8_t expected[32];
  hmac_sha256(g_secret,g_secret_len,raw,raw_bytes,expected);

  if(sig_bytes!=32 || !timing_safe_eq(sig,expected,32)){
    free(raw); free(sig);
    return NULL;
  }

  char *raw_str=(char*)malloc(raw_bytes+1);
  if(!raw_str){ free(raw); free(sig); return NULL; }
  memcpy(raw_str,raw,raw_bytes);
  raw_str[raw_bytes]=0;

  free(raw); free(sig);
  return raw_str;
}

static void sha256_bytes(const uint8_t *in, size_t inlen, uint8_t out[32]){
  sha256_ctx c; sha256_init(&c); sha256_update(&c,in,inlen); sha256_final(&c,out);
}

static void mailing_list_unsubscribe(const char *email){
  (void)email; /* stub */
}

static int respond_generic(struct MHD_Connection *connection){
  struct MHD_Response *resp = MHD_create_response_from_buffer(
    strlen(GENERIC_JSON), (void*)GENERIC_JSON, MHD_RESPMEM_PERSISTENT
  );
  MHD_add_response_header(resp, "Content-Type", "application/json; charset=utf-8");
  MHD_add_response_header(resp, "Cache-Control", "no-store");
  int ret = MHD_queue_response(connection, MHD_HTTP_OK, resp);
  MHD_destroy_response(resp);
  return ret;
}

static int handle_unsubscribe(const char *token){
  if(!token) return 0;

  char *raw = verify_token_get_raw(token);
  if(!raw) return 0;

  uint8_t token_hash[32];
  sha256_bytes((uint8_t*)raw, strlen(raw), token_hash);
  free(raw);

  // Atomic single-use claim + fetch email
  const char *sql_claim =
    "UPDATE unsubscribe_tokens "
    "SET used_at = strftime('%Y-%m-%dT%H:%M:%SZ','now') "
    "WHERE token_hash = ?1 "
    "  AND used_at IS NULL "
    "  AND expires_at > strftime('%Y-%m-%dT%H:%M:%SZ','now') "
    "RETURNING email;";

  sqlite3_stmt *st=NULL;
  if(sqlite3_prepare_v2(g_db, sql_claim, -1, &st, NULL)!=SQLITE_OK) return 0;
  sqlite3_bind_blob(st, 1, token_hash, 32, SQLITE_STATIC);

  char email_buf[512]={0};
  int got=0;
  int rc = sqlite3_step(st);
  if(rc==SQLITE_ROW){
    const unsigned char *e = sqlite3_column_text(st,0);
    if(e){
      snprintf(email_buf,sizeof(email_buf),"%s",(const char*)e);
      normalize_email(email_buf);
      got=1;
    }
  }
  sqlite3_finalize(st);

  if(!got) return 0;

  // Unsubscribe in DB (no enumeration)
  const char *sql_unsub = "UPDATE subscribers SET subscribed = 0 WHERE email = ?1;";
  if(sqlite3_prepare_v2(g_db, sql_unsub, -1, &st, NULL)!=SQLITE_OK) return 1; // token consumed; still generic ok
  sqlite3_bind_text(st, 1, email_buf, -1, SQLITE_TRANSIENT);
  sqlite3_step(st);
  sqlite3_finalize(st);

  mailing_list_unsubscribe(email_buf);
  return 1;
}

static int router(void *cls, struct MHD_Connection *connection,
                  const char *url, const char *method, const char *version,
                  const char *upload_data, size_t *upload_data_size, void **con_cls){
  (void)cls; (void)version; (void)upload_data; (void)upload_data_size; (void)con_cls;

  if(strcmp(method,"GET")!=0) return respond_generic(connection);

  if(strcmp(url,"/unsubscribe")==0){
    const char *token = MHD_lookup_connection_value(connection, MHD_GET_ARGUMENT_KIND, "token");
    // Perform best-effort processing but never change response.
    (void)handle_unsubscribe(token);
    return respond_generic(connection);
  }

  return respond_generic(connection);
}

static void die(const char *msg){
  fprintf(stderr,"%s\n",msg);
  exit(1);
}

int main(void){
  const char *secret = getenv("TOKEN_HMAC_SECRET");
  if(!secret) secret = "change-me-please";
  g_secret_len = strlen(secret);
  if(g_secret_len>sizeof(g_secret)) g_secret_len=sizeof(g_secret);
  memcpy(g_secret, secret, g_secret_len);

  if(sqlite3_open("app.db",&g_db)!=SQLITE_OK) die("failed to open sqlite db");

  const char *schema =
    "CREATE TABLE IF NOT EXISTS unsubscribe_tokens ("
    " token_hash BLOB PRIMARY KEY,"
    " email TEXT NOT NULL,"
    " expires_at TEXT NOT NULL,"
    " used_at TEXT"
    ");"
    "CREATE TABLE IF NOT EXISTS subscribers ("
    " email TEXT PRIMARY KEY,"
    " subscribed INTEGER NOT NULL DEFAULT 1"
    ");";
  char *err=NULL;
  if(sqlite3_exec(g_db, schema, NULL, NULL, &err)!=SQLITE_OK){
    fprintf(stderr,"schema error: %s\n", err?err:"");
    sqlite3_free(err);
    die("schema init failed");
  }

  struct MHD_Daemon *d = MHD_start_daemon(
    MHD_USE_INTERNAL_POLLING_THREAD,
    8080,
    NULL,NULL,
    &router,NULL,
    MHD_OPTION_END
  );
  if(!d) die("failed to start http server");
  fprintf(stderr,"listening on http://127.0.0.1:8080\n");
  getchar();

  MHD_stop_daemon(d);
  sqlite3_close(g_db);
  return 0;
}