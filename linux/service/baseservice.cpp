#include "baseservice.h"
#include "model/servicedata.h"
#include "servicemanager.h"
#include <QDir>
#include <QProcess>
#include <utils.h>


BaseService::BaseService(AppEnum::SERVICE_TYPE type, int profileId, QObject *parent)
    : QObject(parent),
      m_type(type),
      m_profileId(profileId),
      m_drive(nullptr)
{
    m_workerThread = new QThread();
    dynamic_cast<QObject*>(this)->moveToThread(m_workerThread);
    connect(m_workerThread, &QThread::started, this, &BaseService::onThreadStarted);
    connect(m_workerThread, &QThread::finished, this, &BaseService::onThreadFinished);
}

BaseService::~BaseService()
{
    if(nullptr != m_workerThread) {
        delete m_workerThread;
        m_workerThread = nullptr;
    }

    if(nullptr != m_service_data) {
        delete m_service_data;
        m_service_data = nullptr;
    }

    if(main_process_repeater != nullptr) {
        delete main_process_repeater;
        main_process_repeater = nullptr;
    }
}

int BaseService::type()
{
    return m_type;
}

void BaseService::start()
{
    m_workerThread->start();
}

void BaseService::dispose()
{
    m_workerThread->quit();
}

void BaseService::startMainProcess()
{
    if(!main_process_repeater->isActive())
        main_process_repeater->start();
}

void BaseService::stopMainProcess()
{
    if(main_process_repeater->isActive())
        main_process_repeater->stop();
}

ServiceData *BaseService::serviceData()
{
    return m_service_data;
}

void BaseService::setServiceData(ServiceData* data)
{
    m_service_data = data;
}

void BaseService::onThreadStarted()
{
    LOGD;
    if(main_process_repeater == nullptr) {
        main_process_repeater = new QTimer();
        main_process_repeater->setInterval(2000);
        main_process_repeater->setSingleShot(false);
        connect(main_process_repeater, &QTimer::timeout, this, &BaseService::onMainProcess);
    }

    onStarted();
    emit started(this);
}

void BaseService::onThreadFinished()
{
    LOGD;
    emit finished(this);
}

void BaseService::finish()
{
    LOGD;
    stopMainProcess();
    dispose();
}
