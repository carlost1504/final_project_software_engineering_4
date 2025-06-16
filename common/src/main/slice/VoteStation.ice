module common {
    sequence<string> CandidateList;

    interface VoteStation {
        bool vote(string document, int candidateId, int stationId, string hmac);
        void generateReport();
        CandidateList getCandidates();
    };
};
