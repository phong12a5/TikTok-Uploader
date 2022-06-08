#ifndef BASESERVICE_H
#define BASESERVICE_H

#include <QObject>
#include <QThread>
#include "log.h"
#include "model/cloneinfo.h"
#include "fdriver/include/fdriver.h"
#include "fdriver/include/browsers/chrome.h"
#include <QTimer>

using namespace fdriver;

class ServiceData;

class BaseService : public QObject
{
    Q_OBJECT
public:
    enum SERVICE_TYPE: int {
      TYPE_CHROME_SERVICE = 0,
      TYPE_FIREFOX_SERVICE
    };

    enum BY : int {
        BY_NAME = 0,
        BY_CLASS,
        BY_XPATH,
        BY_LINK_TEXT,
        BY_ID
    };
public:
    explicit BaseService(SERVICE_TYPE type, int profileId, QObject *parent = nullptr);
    virtual ~BaseService();

    int type();
    void start();
    void dispose();
    void startMainProcess();
    void stopMainProcess();

    ServiceData* serviceData();
    void setServiceData(ServiceData* data);

signals:
    void serviceFinished(int serviceId);

private slots:
    void onThreadStarted();
    void onThreadFinished();

public slots:
    virtual void onStarted() = 0;
    virtual void onMainProcess() = 0;

    //Interface
protected:
    virtual void connectSignalSlots() = 0;
    void setCookies(QString cookies);
    QString getCookies(bool* ok = nullptr);
    bool inputText(QString textInput, By by);
    bool click(By by);
    bool ElementExist(const fdriver::By &by);
    bool FindElement(Element& element, const fdriver::By &by);

    void finish();

protected:
    QThread* m_workerThread = nullptr;
    ServiceData* m_service_data = nullptr;
    QTimer* main_process_repeater = nullptr;
    int m_type;
    int m_profileId;

protected:
    FDriver *driver = nullptr;

signals:
    void finished(BaseService* /*this*/);
    void started(BaseService* /*this*/);
};

#endif // BASESERVICE_H
