#ifndef CHROMESERVICE_H
#define CHROMESERVICE_H

#include <QObject>
#include "baseservice.h"
#include <model/servicedata.h>
//#include "worker/chromeworker.h"

class ServiceData;
class AFAction;

class ChromeService : public BaseService
{
    Q_OBJECT
public:
    explicit ChromeService(int profileId, QObject *parent = nullptr);
    ~ChromeService();

    void connectSignalSlots() override;
    ServiceData* model();

private:
    void initChromeDriver();
    void getProxy();
    void getClone();
    void getActions();

    void login();
    void feedLike(bool acceptLike);
    bool followByPage(QString pageId, AFAction* action);
    bool getPagesOfUid();
    bool getFb_dtsg();


    bool checkProxy(PROXY proxy);
    bool getInviteLink(QJsonObject& data, QString uid);
    bool acceptInvitation(QJsonObject& data);
    bool submitAcceptedInvitation(QJsonObject link);

    int detectScreen();

public slots:
    void onStarted() override;
    void onMainProcess() override;

private:
    bool m_checkInvLink;
    bool m_getPageList;
    QString m_fb_dtsg;
    QString m_jazoest;
    QList<int> m_screen_stack;
};

#endif // CHROMESERVICE_H
