#ifndef SERVICEDATA_H
#define SERVICEDATA_H

#include <QObject>
#include "cloneinfo.h"
#include "log.h"
#include <array>
#include <array>
#include "AppEnum.h"
#include "cloneinfo.h"
#include <AppDefine.h>
#include <QSize>
#include <QPoint>
#include <QJsonArray>

class AFAction;

typedef struct proxy{
    AppEnum::E_PROXY_TYPE type;
    QString ip;
    int port;
    QString username;
    QString password;
    proxy(AppEnum::E_PROXY_TYPE _type, QString _ip, int _port, QString _username = "", QString _password = "") {
        type = _type;
        ip = _ip;
        port = _port;
        username = _username;
        password = _password;
    }

    proxy(const proxy& other) :
        type(other.type),
        ip(other.ip),
        port(other.port),
        username(other.username),
        password(other.password){ }

    std::string toString() {
        if(type == AppEnum::E_SOCKS5_PROXY)
            return std::string("socks5://") + ip.toStdString() + ":" + std::to_string(port);
        else {
            return std::string("http://") + ip.toStdString() + ":" + std::to_string(port);
        }
    }
} PROXY;

class ServiceData : public QObject
{
    Q_OBJECT

public:

    enum ACTION : int {
        ACTION_LOGIN = 0,
        ACTION_SCROLL_FEED,
        ACCTION_FEED_LIKE,
        ACCTION_NEWFEED_BUTTON,
        ACCTION_FRIEND_BUTTON
    };

    explicit ServiceData(AppEnum::SERVICE_TYPE type, int profileId, QObject* parent = nullptr);
    ~ServiceData();

    QString profilePath() { return m_profilePath; }

    QString cloneInfokey() { return QString(CLONE_INFO_FILED).arg(m_type_str).arg(m_profileId);}

    CloneInfo* cloneInfo();
    void  setCloneInfo(CloneInfo* cloneInfo);

    void setLinkProfile(QString url) {
        m_linkProfile = url;
    }

    QString getLinkProfile() {
        return m_linkProfile;
    }

    int numberChrom() {
        return m_numberThread;
    }

    QPoint windowPosition() {
        return m_posstion;
    }

    void setWindowPosition(QPoint pos) {
        m_posstion = pos;
    }

    QSize windowSize() {
        return m_windowSize;
    }

    void setWindowSize(QSize size) {
        m_windowSize = size;;
    }

    void setServiceID(int num) {
        m_serviceID = num;
    }
    int getServiceID() {
        return m_serviceID;
    }

    void setProxy(PROXY& proxy) {
        m_proxy = new PROXY(proxy);
    }
    PROXY* getProxy() {
        return m_proxy;
    }

    int actionsSize();
    AFAction* getRandomAction();
    QList<AFAction*>* getActionList();
    void setActionsList(QJsonArray array);
private:
    void loadCloneInfo();

private slots:
    void onCloneInfoChanged(QString action = "");

private:
    AppEnum::SERVICE_TYPE m_type;
    QString m_type_str;
    int m_profileId;
    QString m_profileFolderPath;
    QString m_profilePath;
    CloneInfo* m_cloneInfo;
    std::string m_2Fa;
    int m_numberThread;
    int m_xPosstion;
    int m_yPossition;
    QString m_linkImage;
    int m_serviceID;
    PROXY* m_proxy;
    QString m_linkProfile;
    QPoint m_posstion;
    QSize m_windowSize;
    QList<AFAction*>* m_actions;
};

#endif // SERVICEDATA_H
