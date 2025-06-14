module common {
    interface VoteStation;
    interface QueryStation;

    interface LoadBalancer {
        VoteStation* getVoteStation();
        QueryStation* getQueryStation();
        void addVoteStation(VoteStation* station);
        void addQueryStation(QueryStation* station);
    };
};
