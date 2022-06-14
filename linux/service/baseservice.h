#ifndef BASESERVICE_H
#define BASESERVICE_H

#include <QObject>
#include <QThread>
#include "log.h"
#include "model/cloneinfo.h"
#include <QTimer>
#include <AppEnum.h>

class ServiceData;

class BaseService : public QObject
{
    Q_OBJECT

public:
    explicit BaseService(AppEnum::SERVICE_TYPE type, int profileId, QObject *parent = nullptr);
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
    void finish();

protected:
    QThread* m_workerThread = nullptr;
    ServiceData* m_service_data = nullptr;
    QTimer* main_process_repeater = nullptr;
    int m_type;
    int m_profileId;

protected:
   void* m_drive;

signals:
    void finished(BaseService* /*this*/);
    void started(BaseService* /*this*/);
};

#endif // BASESERVICE_H
