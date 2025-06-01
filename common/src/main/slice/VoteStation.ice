module common {
    interface VoteStation {
        bool vote(string document, int candidateId);
        void generateReport();
    };
};