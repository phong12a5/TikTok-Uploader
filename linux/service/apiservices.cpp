#include "apiservices.h"
#include "log.h"
#include <appmodel.h>
#include <QJsonDocument>
#include <QJsonObject>


static APIServices* sInstance = nullptr;

APIServices *APIServices::instance()
{
    if(!sInstance) {
        sInstance = new APIServices();
    }
    return sInstance;
}

APIServices::APIServices(QObject *parent) : QObject(parent)
{
    m_checker = nullptr;
}

void APIServices::startService()
{
    m_subThread = new QThread();
    this->moveToThread(m_subThread);
    connect(m_subThread , &QThread::started, this, &APIServices::onStarted);
    m_subThread->start();
}

bool APIServices::isFdriverReady()
{
    return m_isFdriverReady;
}

bool APIServices::isAFAPIReady()
{
    return m_isAFAPIReady;
}

bool APIServices::isDeviceApproved()
{
    return m_config.value("device_info").toObject().value("status") == "Approved";
}

QString APIServices::deviceStatus()
{
    return m_config.value("device_info").toObject().value("status").toString();
}

QJsonObject APIServices::config()
{
    return m_config;
}

void APIServices::setConfig(QJsonObject config)
{
    if(m_config != config) {
        m_config = config;
        emit configChanged();
    }
}

void APIServices::onStarted()
{
    LOGD;
}
