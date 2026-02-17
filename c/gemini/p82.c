// Simplified logic for route handling and JSON parsing
static enum MHD_Result handler(void *cls, struct MHD_Connection *conn, ...) {
    const char *payload = MHD_lookup_connection_value(conn, MHD_GET_ARGUMENT_KIND, "payload");
    json_t *root = json_loads(payload, 0, NULL);
    const char *name = json_string_value(json_object_get(root, "name"));
    return send_response(conn, name);
}