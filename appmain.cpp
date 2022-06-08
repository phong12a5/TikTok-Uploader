#include "appmain.h"
#include "service/servicemanager.h"
#include "service/chromeservice.h"
#include "fdriver.h"
#include "appmodel.h"
#include <QCoreApplication>
#include <QDir>
#include <WebAPI.hpp>
#include <service/apiservices.h>
#include <AppDefine.h>
#include <model/servicedata.h>

using namespace fdriver;

AppMain* AppMain::m_instance = NULL;

AppMain::AppMain(QObject *parent) : QObject(parent)
{
    QCoreApplication::setOrganizationName("AutoFarmer");
    QCoreApplication::setOrganizationDomain("autofarmer.net");
    QCoreApplication::setApplicationName("Subscribe Tool");

    connect(ServiceManager::instance(), &ServiceManager::serviceUpdated, this, &AppMain::onServiceUpdated);
    connect(APIServices::instance(), &APIServices::configChanged, this, &AppMain::onConfigChanged);
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
    if(m_preconditionChecker == nullptr) {
        m_preconditionChecker = new QTimer();
        m_preconditionChecker->setSingleShot(false);
        m_preconditionChecker->setInterval(2000);
        connect(m_preconditionChecker, &QTimer::timeout, this, &AppMain::onCheckPrecondition);
    }
    m_preconditionChecker->start();
    AppModel::instance()->setAppStarted(true);
    return true;
}

bool AppMain::stop()
{
    m_preconditionChecker->stop();
    foreach(BaseService* serviceId, ServiceManager::instance()->getServiceIds()) {
        ServiceManager::instance()->stopService(serviceId);
    }
    AppModel::instance()->setAppStarted(false);
    return true;
}

void AppMain::setWindowProp(ServiceData* model, int index)
{
    int serviceCount = AppModel::instance()->runningBrowser();
    int screenWidth = AppModel::instance()->screen_width();
//    int screenHeight = AppModel::instance()->screen_height();
    float screenRatio = 4/3;

    int targetWidth = screenWidth/serviceCount, targetHeight = targetWidth * screenRatio;
    model->setWindowSize(QSize(targetWidth, targetHeight));
    model->setWindowPosition(QPoint(targetWidth * index, 0));
}

void AppMain::onCheckPrecondition()
{
    if(!APIServices::instance()->isFdriverReady()) {
        LOGD << "Fdriver is not ready";
    } else if(!APIServices::instance()->isAFAPIReady()) {
        LOGD << "Autofarmer APIs is not ready";
    } else if(!APIServices::instance()->isDeviceApproved()) {
        LOGD << "Device is not approved";
    } else {
        if(m_chromeDriverProcess == nullptr) {
            m_chromeDriverProcess = new QProcess(this);
            m_chromeDriverProcess->setWorkingDirectory(QDir::currentPath());
            m_chromeDriverProcess->setProgram("chromedriver.exe");
            connect(m_chromeDriverProcess, &QProcess::errorOccurred, this, [=](QProcess::ProcessError error){
                LOGD << error;
            });
            m_chromeDriverProcess->start();
            m_chromeDriverProcess->waitForStarted(-1);
        }
        emit ServiceManager::instance()->serviceUpdated();
    }
}

void AppMain::onServiceUpdated()
{
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

void AppMain::onConfigChanged()
{
    AppModel::instance()->setDeviceStatus(APIServices::instance()->deviceStatus().isEmpty()? "checking" : APIServices::instance()->deviceStatus());
}
