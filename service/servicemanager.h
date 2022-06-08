#ifndef SERVICEMANAGER_H
#define SERVICEMANAGER_H

#include <QObject>
#include "baseservice.h"
//#include "controllermainthread.h"

class ServiceManager : public QObject
{
    Q_OBJECT
public:
    static ServiceManager* instance();
    void start();
    void stop();

    QList<BaseService*> getServiceIds();
    int countService();
    void stopService(BaseService * serviceId);
    QList<BaseService *> listService();

    template<typename T>
    T* createService(int profileId) {
        T* service = new T(profileId);
        LOGD << " : " << service;
        m_listService.append(service);
        connect(service, &BaseService::started, this,&ServiceManager::onServiceStarted);
        connect(service, &BaseService::finished, this,&ServiceManager::onServiceFinished);
        return service;
    }

public slots:
    void onServiceStarted(BaseService* service);
    void onServiceFinished(BaseService* service);

signals:
    void serviceUpdated();

private:
    explicit ServiceManager(QObject *parent = nullptr);
    ~ServiceManager();
private:
    static ServiceManager* sInstance;

    QList<BaseService*> m_listService;

signals:
};

#endif // SERVICEMANAGER_H
