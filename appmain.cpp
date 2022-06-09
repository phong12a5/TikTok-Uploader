#include "appmain.h"
#include "service/servicemanager.h"
#include "service/chromeservice.h"
#include "appmodel.h"
#include <QCoreApplication>
#include <QDir>
#include <service/apiservices.h>
#include <AppDefine.h>
#include <model/servicedata.h>


AppMain* AppMain::m_instance = NULL;

AppMain::AppMain(QObject *parent) :
    QObject(parent),
    m_chromeDriverProcess(nullptr)
{
    QCoreApplication::setOrganizationName("AutoFarmer");
    QCoreApplication::setOrganizationDomain("autofarmer.net");
    QCoreApplication::setApplicationName("Subscribe Tool");

    connect(ServiceManager::instance(), &ServiceManager::serviceUpdated, this, &AppMain::onServiceUpdated);
    APIServices::instance()->startService();
}

AppMain *AppMain::instance()
{
    if(m_instance == NULL) {
        m_instance = new AppMain();
    }
    return m_instance;
}

bool AppMain::start()
{
    LOGD;
    AppModel::instance()->setAppStarted(true);

    if(m_chromeDriverProcess == nullptr)
        m_chromeDriverProcess = new QProcess(this);
    m_chromeDriverProcess->setWorkingDirectory(QDir::currentPath());
    m_chromeDriverProcess->setProgram("./chromedriver");
    connect(m_chromeDriverProcess, &QProcess::errorOccurred, this, [=](QProcess::ProcessError error){
        LOGD << error;
    });
    m_chromeDriverProcess->start();
    m_chromeDriverProcess->waitForStarted(-1);

    emit ServiceManager::instance()->serviceUpdated();
    return true;
}

bool AppMain::stop()
{
    m_chromeDriverProcess->kill();
    foreach(BaseService* serviceId, ServiceManager::instance()->getServiceIds()) {
        ServiceManager::instance()->stopService(serviceId);
    }
    AppModel::instance()->setAppStarted(false);
    return true;
}

void AppMain::onServiceUpdated()
{
    LOGD;
    if(AppModel::instance()->appStarted()) {
        if(ServiceManager::instance()->countService() < AppModel::instance()->maxThread() && \
                ServiceManager::instance()->countService() < MAX_PROFILE_NUMBER) {
            int nextProfileId = AppModel::instance()->latestProfileId();
            if(nextProfileId >= MAX_PROFILE_NUMBER) {
                nextProfileId = 1;
            } else {
                nextProfileId ++;
            }
            AppModel::instance()->setLatestProfileId(nextProfileId);
            ChromeService* service = ServiceManager::instance()->createService<ChromeService>(nextProfileId);
            service->start();
        }
    }
}
