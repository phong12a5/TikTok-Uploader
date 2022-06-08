#ifndef APPENUM_H
#define APPENUM_H

// do not include anything
#include <QString>

class AppEnum {

public:

    enum E_PROXY_TYPE: int {
        E_HTTP_PROXY = 0,
        E_SOCKS5_PROXY
    };

    enum E_SCREEN_ID: int {
        E_SCREEN_LOGIN = 0,
        E_SCREEN_ENTER_LOGIN_CODE,
        E_SCREEN_SAVE_BROWSER,
        E_SCREEN_CREATE_SHORTCUT,
        E_SCREEN_CHECKPOINT,
        E_SCREEN_HOME,

        E_SCREEN_UNKNOWN
    };

    static QString scrIdStr(int id) {
        switch (id) {
        case E_SCREEN_ID::E_SCREEN_LOGIN:
            return "E_SCREEN_LOGIN";
        case E_SCREEN_ID::E_SCREEN_ENTER_LOGIN_CODE:
            return "E_SCREEN_ENTER_LOGIN_CODE";
        case E_SCREEN_ID::E_SCREEN_SAVE_BROWSER:
            return "E_SCREEN_SAVE_BROWSER";
        case E_SCREEN_ID::E_SCREEN_CREATE_SHORTCUT:
            return "E_SCREEN_CREATE_SHORTCUT";
        case E_SCREEN_ID::E_SCREEN_CHECKPOINT:
            return "E_SCREEN_CHECKPOINT";
        case E_SCREEN_ID::E_SCREEN_HOME:
            return "E_SCREEN_HOME";
        case E_SCREEN_ID::E_SCREEN_UNKNOWN:
            return "E_SCREEN_UNKNOWN";
        default:
            return "UNIMPLEMENTED SCREEN";
        }
    }
};

#endif // APPENUM_H
