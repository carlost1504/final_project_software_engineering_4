module common {

    interface QueryCache {
        string query(string document);
        idempotent void invalidate(string document);
    };
};