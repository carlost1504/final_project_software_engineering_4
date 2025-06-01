module common {
    interface VoteStation {
        bool vote(string document, int candidateId, int stationId);
        void generateReport();
    };
};