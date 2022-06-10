#ifndef APPDEFINE_H
#define APPDEFINE_H

/******************************************App Define******************************************/
#ifdef __DEBUG_MODE__
#define APP_VER "1.0.12"
#else
#define APP_VER "1.0.74"
#endif
#define DATABASE_VER "1.00.02"

/*********************SQLite*********************/
#define DATABASE_NAME_DEFAULT "db_maxcare.sqlite"
#define DEFAULT_KEY "Congaubeo@12345Congaubeo@5678910"
#define DEFAULT_IV "Congaubeo@555555"


#define MAX_PROFILE_NUMBER          1

#define MAX_SCREEN_LOOP 10

#define MAX_THREAD_FIELD            "config/max_thread"
#define TOKEN_FIELD                 "config/token"
#define LATEST_PROFILE_ID_FIELD     "config/latest_profile_id"

#define CLONE_INFO_FILED "data/%1/clone_info/%2"

#define FACEBOOK_APP "facebook"

#endif // APPDEFINE_H
