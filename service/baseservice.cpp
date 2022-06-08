#include "baseservice.h"
#include "model/servicedata.h"
#include "servicemanager.h"
#include <QDir>
#include <QProcess>
#include <utils.h>

BaseService::BaseService(SERVICE_TYPE type, int profileId, QObject *parent)
    : QObject(parent),
      m_type(type),
      m_profileId(profileId)
{
    m_workerThread = new QThread();
    this->moveToThread(m_workerThread);
    connect(m_workerThread, &QThread::started, this, &BaseService::onThreadStarted);
    connect(m_workerThread, &QThread::finished, this, &BaseService::onThreadFinished);
}

BaseService::~BaseService()
{
    if(nullptr != driver) {
        delete driver;
    }

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

void BaseService::setCookies(QString cookies)
{
    LOGD;
    std::string c_user = "";
    std::string xs = "";
    std::string fr = "";
    std::string datr = "";
    try {
        if(cookies.contains(";")) {
            QStringList cookiear = cookies.split(";");
            for (int i= 0; i<cookiear.size() ;i++ ) {
                QStringList cookiearr = cookiear[i].split("=");
                for (auto i: cookiearr) {
                    if (cookiearr[0].trimmed() == "c_user") {
                        std::string c_userarr = cookiearr[1].toStdString();
                        c_user = c_userarr;
                        Cookie cookie1("c_user", c_user, "/", "");
                        driver-> SetCookie(cookie1);
                    }
                    if (cookiearr[0].trimmed() == "xs") {

                        std::string xsarr = cookiearr[1].toStdString();
                        xs = xsarr;
                        Cookie cookie2("xs", xs, "/", "");
                        driver-> SetCookie(cookie2);
                    }
                    if (cookiearr[0].trimmed() == "fr") {

                        std::string frarr = cookiearr[1].toStdString();
                        fr = frarr;
                        Cookie cookie3("fr", fr, "/", "");
                        driver-> SetCookie(cookie3);
                    }
                    if (cookiearr[0].trimmed() == "datr") {

                        std::string datrarr = cookiearr[1].toStdString();
                        datr = datrarr;
                        Cookie cookie4("datr", datr, "/", "");
                        driver-> SetCookie(cookie4);
                    }
                }
            }
        }
    } catch (const std::exception& ex) {
        LOGD << "cant not finl element";
    } catch (const std::string& ex) {
        LOGD << "cant not finl element";
    } catch (...) {
        LOGD << "cant not finl element";
    }

}

QString BaseService::getCookies(bool* ok)
{
    try {
        std::vector<Cookie> cookies = driver->GetCookies();
        std::string cookiesStr;
        foreach(Cookie cookie , cookies) {
            std::string cookieStr;
            if(cookiesStr.empty()) {
                cookieStr += cookie.name + "=" + cookie.value;
            } else {
                cookieStr += ";" + cookie.name + "=" + cookie.value;
            }
            cookiesStr += cookieStr;
        }
        if(ok) *ok = true;
        return QString(cookiesStr.c_str());
    } catch(...) {
        if(ok) *ok = false;
        return QString();
    }
}

bool BaseService::inputText(QString textInput, By by)
{
    try {
        auto parts = textInput.split(QString());
        Element element = driver->FindElement(by);
        for (int i = 1; i < parts.length(); i++) {
            delay(random(400, 500));
            element.SendKeys(parts[i].toStdString());
        }
        return true;
    } catch (const std::exception& ex) {
        LOGD << ex.what();
        return false;
    }
}

bool BaseService::click(By by)
{
    try {
        Element element = driver->FindElement(by);
        element.Click();
        return true;
    } catch (const std::exception& ex) {
        LOGD << ex.what();
        return false;
    }
}

bool BaseService::ElementExist(const fdriver::By &by)
{
    try {
        driver->FindElement(by);
        return true;
    } catch(...) {
        LOGD << by.GetStrategy().c_str() << ":" << by.GetValue().c_str() << " not found";
        return false;
    }
}

bool BaseService::FindElement(Element &element, const By &by)
{
    try {
        element = driver->FindElement(by);
        return true;
    } catch(...) {
        return false;
    }
}

void BaseService::finish()
{
    LOGD;
    stopMainProcess();
    dispose();
}
