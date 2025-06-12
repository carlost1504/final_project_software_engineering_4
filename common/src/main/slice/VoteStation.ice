module common {
    interface VoteStation {
        bool vote(string document, int candidateId, int stationId, string hmac);
        void generateReport();
    };
};