#include "appmodel.h"
#include <QCoreApplication>
#include "log.h"
#include "AppDefine.h"
#include <QApplication>
#include <QScreen>
#include <QSettings>
AppModel* AppModel::sInstance = nullptr;

AppModel *AppModel::instance()
{
    if(sInstance == nullptr) {
        sInstance = new AppModel();
    }
    return sInstance;
}

AppModel::AppModel() :
    m_appStarted(false)
{
    QSettings settings;
    if(settings.contains(MAX_THREAD_FIELD))
        m_maxThread = settings.value(MAX_THREAD_FIELD).toInt();
    else
        m_maxThread = 1;
}


bool AppModel::appStarted() { return m_appStarted; }
void AppModel::setAppStarted(bool state) {
    if(m_appStarted != state) {
        m_appStarted = state;
        emit appStartedChanged();
    }
}

int AppModel::maxThread() { return m_maxThread; }
void AppModel::setMaxThread(int max) {
    if(max != m_maxThread) {
        m_maxThread = max;
        QSettings settings;
        settings.setValue(MAX_THREAD_FIELD, max);
        emit maxThreadChanged();
    }
}

QString AppModel::deviceName()
{
    return QSysInfo::machineHostName();
}

QString AppModel::appVersion()
{
    return "0.0.1";
}

int AppModel::latestProfileId()
{
    QSettings settings;
    if(settings.contains(LATEST_PROFILE_ID_FIELD)) {
        return settings.value(LATEST_PROFILE_ID_FIELD).toInt();
    } else {
        return 0;
    }
}

void AppModel::setLatestProfileId(int id)
{
    QSettings settings;
    settings.setValue(LATEST_PROFILE_ID_FIELD, id);
}

int AppModel::screen_width()
{
    return QGuiApplication::primaryScreen()->geometry().width();
}

int AppModel::screen_height()
{
    return QGuiApplication::primaryScreen()->geometry().height();
}
