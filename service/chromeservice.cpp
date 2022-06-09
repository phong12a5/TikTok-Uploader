#include "chromeservice.h"
#include "model/servicedata.h"
#include <QTimer>
#include <CkHttp.h>
#include <CkJsonObject.h>
#include <QJsonArray>
#include <QFile>
#include <QProcess>
#include <utils.h>
#include <regex>
#include <CkHttp.h>
#include <CkHttpRequest.h>
#include <CkHttpResponse.h>
#include <QRandomGenerator>
#include <appmodel.h>
#include <QDir>
#include <QJsonArray>
#include "model/afaction.h"
#include <exception>
#include <stdexcept>
#include <AppEnum.h>
#include <QEventLoop>
#include <QNetworkReply>
#include <QHttpMultiPart>
#include <QHttpPart>
#include <QNetworkAccessManager>
#include <QNetworkRequest>

#include <webdriverxx.h>

using namespace webdriverxx;


QString getRandomUserAgent()
{
    QString userAgen;
    QFile file;
    file.setFileName(":/model/userAgent.json");
    file.open(QIODevice::ReadOnly | QIODevice::Text);
    userAgen = file.readAll();
    file.close();

    QJsonDocument jsonDocUserAgen = QJsonDocument::fromJson(userAgen.toUtf8());
    QJsonObject jsonUserAgent = jsonDocUserAgen.object();
    QJsonValue infoUserAgent = jsonUserAgent.value(QString("useragent"));
    QJsonArray listUserAgen = infoUserAgent.toArray();
    int leng = infoUserAgent.toArray().size() - 1;
    srand(time(NULL));
    int res = rand() % leng;
    return listUserAgen.at(res).toString();
}

bool inputText(webdriverxx::WebDriver* driver, QString textInput,const webdriverxx::By& by)
{
    try {
        auto parts = textInput.split(QString());
        webdriverxx::Element element = driver->FindElement(by);
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

bool click(webdriverxx::WebDriver* driver, webdriverxx::By& by)
{
    try {
        webdriverxx::Element element = driver->FindElement(by);
        element.Click();
        return true;
    } catch (const std::exception& ex) {
        LOGD << ex.what();
        return false;
    }
}

bool ElementExist(webdriverxx::WebDriver* driver, const webdriverxx::By &by)
{
    try {
        driver->FindElement(by);
        return true;
    } catch(...) {
        LOGD << by.GetStrategy().c_str() << ":" << by.GetValue().c_str() << " not found";
        return false;
    }
}

bool FindElement(webdriverxx::WebDriver* driver, webdriverxx::Element &element, const webdriverxx::By &by)
{
    try {
        element = driver->FindElement(by);
        return true;
    } catch(...) {
        return false;
    }
}

void setCookies(webdriverxx::WebDriver* driver, QString cookies)
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
                        webdriverxx::Cookie cookie1("c_user", c_user, "/", "");
                        driver->SetCookie(cookie1);
                    }
                    if (cookiearr[0].trimmed() == "xs") {

                        std::string xsarr = cookiearr[1].toStdString();
                        xs = xsarr;
                        webdriverxx::Cookie cookie2("xs", xs, "/", "");
                        driver->SetCookie(cookie2);
                    }
                    if (cookiearr[0].trimmed() == "fr") {

                        std::string frarr = cookiearr[1].toStdString();
                        fr = frarr;
                        webdriverxx::Cookie cookie3("fr", fr, "/", "");
                        driver->SetCookie(cookie3);
                    }
                    if (cookiearr[0].trimmed() == "datr") {

                        std::string datrarr = cookiearr[1].toStdString();
                        datr = datrarr;
                        webdriverxx::Cookie cookie4("datr", datr, "/", "");
                        driver->SetCookie(cookie4);
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

QString getCookies(webdriverxx::WebDriver* driver, bool* ok)
{
    try {
        std::vector<webdriverxx::Cookie> cookies = driver->GetCookies();
        std::string cookiesStr;
        foreach(webdriverxx::Cookie cookie , cookies) {
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

ChromeService::ChromeService(int profileId, QObject *parent) :
    BaseService(AppEnum::TYPE_CHROME_SERVICE,profileId, parent)
{
    m_checkInvLink = false;
    m_getPageList = false;
    LOGD << "--------- ChromeService: " << profileId << " ---------";
}

ChromeService::~ChromeService()
{
    if(nullptr != m_drive) {
        delete static_cast<webdriverxx::WebDriver*>(m_drive);
    }
}

void ChromeService::connectSignalSlots()
{
//    LOGD;
}

ServiceData *ChromeService::model()
{
    return m_service_data;
}

void ChromeService::initChromeDriver()
{
    LOGD;
    webdriverxx::Chrome chrome;
    webdriverxx::ChromeOptions chromeOptions;
    chrome.SetPlatform(webdriverxx::platform::Mac);

    std::vector<std::string> args;
    args.push_back("--user-data-dir=" + serviceData()->profilePath().toStdString());
    args.push_back("--ignore-certificate-errors");
//    args.push_back("--proxy-server=" + serviceData()->getProxy()->toString());
    args.push_back("--disable-features=ChromeWhatsNewUI");
//    args.push_back("--headless");

    args.push_back("--no-sandbox");
    args.push_back("--start-maximized");
//    args.push_back("--start-fullscreen");
    args.push_back("--single-process");
    args.push_back("--disable-dev-shm-usage");
//    args.push_back("--incognito");
    args.push_back("--disable-blink-features=AutomationControlled");
    args.push_back("disable-infobars");
//    args.push_back("")

#if 0
    if(serviceData()->cloneInfo()->userAgent().isEmpty()) {
        serviceData()->cloneInfo()->setUserAgent(getRandomUserAgent());
    }
#endif

    args.push_back("--disable-notifications");
//    args.push_back("--window-position=1500,0");
    chromeOptions.SetArgs(args);

    std::vector<std::string> switches;
    switches.push_back("enable-automation");
    switches.push_back("load-extension");
    chromeOptions.SetExcludeSwitches(switches);
    chromeOptions.SetUseAutomationExtension(false);


    webdriverxx::JsonObject sourceJson = webdriverxx::JsonObject();
//    sourceJson.Set("intl.accept_languages", "en,en_US");
//    sourceJson.Set("profile.password_manager_enabled", false);
//    sourceJson.Set("credentials_enable_service", false);
    chromeOptions.SetPrefs(sourceJson);

    chromeOptions.SetBinary("/usr/bin/google-chrome");
    LOGD << "Ahihi";

#if 0
    webdriverxx::chrome::MobileEmulation mobileEmulation;
    mobileEmulation.SetdeviceName("iPhone X");
//    mobileEmulation.SetuserAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 13_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.2 Mobile/15E148 Safari/604.1");
//    mobileEmulation.SetdeviceMetrics(webdriverxx::chrome::device::deviceMetrics().Settouch(true).Setwidth(375).Setheight(812).SetpixelRatio(3));
    chromeOptions.SetMobileEmulation(mobileEmulation);
#endif

    chrome.SetChromeOptions(chromeOptions);

    const char * url ="http://localhost:9515/";
    m_drive = new WebDriver(chrome, webdriverxx::Capabilities(),  url);

//    driver.execute_cdp_cmd("Page.addScriptToEvaluateOnNewDocument", {
//        "source":
//            "const newProto = navigator.__proto__;"
//            "delete newProto.webdriver;"
//            "navigator.__proto__ = newProto;"
//    })

    static_cast<webdriverxx::WebDriver*>(m_drive)->Execute("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
    static_cast<webdriverxx::WebDriver*>(m_drive)->DeleteCookies();
    static_cast<webdriverxx::WebDriver*>(m_drive)->Navigate("https://www.tiktok.com");
}

void ChromeService::getProxy()
{
#if 0
    CkHttp http;

    // Set the Login and Password properties for authentication.
    http.put_Login("admin");
    http.put_Password("w2Yt4b2B8xWnhoa");

    // To use HTTP Basic authentication..
    http.put_BasicAuth(true);

    const char *result = http.quickGetStr("https://proxy.autofarmer.net/public-api/v1/get-vpn");
    LOGD << "result: " << result;
    if (http.get_LastMethodSuccess() != true) {
        std::cout << http.lastErrorText() << "\r\n";
        return;
    }

    if(result) {
        CkJsonObject jsonResult;
        if(jsonResult.Load(result) && jsonResult.HasMember("data")) {
            CkJsonObject* data = jsonResult.ObjectOf("data");
            LOGD << (data && data->HasMember("vpn"));
            if(data && data->HasMember("vpn")) {
                QString proxy = "140.82.45.238:8899";//data->stringOf("vpn");
                QStringList params = proxy.trimmed().split(":");
                if(params.length() == 2) {
                    QString ip = params[0];
                    if(ping(ip)) {
                        serviceData()->setProxy(proxy);
                        return;
                    }
                }
            }
        }
    }
#else
    QString ip = "10.10.243.97";
    int httpPorts[] = {4001, 4002, 4004, 4005, 4006, 6001, 6002, 6003, 6004, 6005, 6006};
#if 0
    int socks5Ports[] = {5001, 5002, 5004, 5005, 5006, 7001, 7002, 7003, 7004, 7005, 7006};
#endif
    int port = httpPorts[QRandomGenerator::global()->bounded(11)];
    PROXY httpProxy(AppEnum::E_HTTP_PROXY, ip, port);
    if(checkProxy(httpProxy)) {
        dynamic_cast<BaseService*>(this)->serviceData()->setProxy(httpProxy);
    }
#endif
}

void ChromeService::getClone()
{
//    std::string result = WebAPI::getInstance()->getClone(nullptr, "facebook");
//    QJsonObject cloneInfo = QJsonDocument::fromJson(result.c_str()).object().value("cloneInfo").toObject();
//    if(!cloneInfo.isEmpty()) {
//        LOGD << cloneInfo;
//        serviceData()->setCloneInfo(new CloneInfo(cloneInfo));
//        if(serviceData()->cloneInfo()) {
//            serviceData()->cloneInfo()->setAliveStatus(CLONE_ALIVE_STATUS_STORE);
//        }
//    }
}

void ChromeService::getActions()
{
//    std::string actionsStr = WebAPI::getInstance()->doAction(nullptr, FACEBOOK_APP, serviceData()->cloneInfo()->cloneId().toUtf8().data());
//    LOGD << actionsStr.c_str();

//    QJsonObject doactionObj = QJsonDocument::fromJson(actionsStr.c_str()).object();
//    QString message = doactionObj.value("message").toString();
//    bool success = doactionObj.value("success").toBool();
//    int code = doactionObj.value("code").toInt();
//    if(success && code == 200) {
//        serviceData()->setActionsList(doactionObj.value("actions").toArray());
//    }
}

void ChromeService::login()
{
    LOGD;
//    try {
//        QString uid = serviceData()->cloneInfo()->uid();
//        QString password = serviceData()->cloneInfo()->password();

//        Element element;
//        if(FindElement(element, ByXPath("//*[contains(@data-sigil, 'm_login_email')]")) && uid != QString(element.GetAttribute("value").c_str())) {
//             inputText(serviceData()->cloneInfo()->uid(),ByXPath("//*[contains(@data-sigil, 'm_login_email')]"));
//             delay(random(500, 1000));
//        }

//        if(FindElement(element, ByXPath("//*[contains(@data-sigil, 'm_login_email')]")) &&
//                       password != QString(element.GetAttribute("value").c_str())) {
//            inputText(serviceData()->cloneInfo()->password(),ByXPath("//*[contains(@data-sigil, 'password-plain-text-toggle-input')]"));
//            delay(random(500, 1000));
//        }

//        if(ElementExist(ByXPath("//*[contains(@data-sigil, 'touchable login_button_block m_login_button')]"))) {
//            click(ByXPath("//*[contains(@data-sigil, 'touchable login_button_block m_login_button')]"));
//            delay(5000);
//        }

//        if(ElementExist(ById("approvals_code"))) {
//            QString secretkey = serviceData()->cloneInfo()->secretkey();
//            if(secretkey.isEmpty()) {
//                serviceData()->cloneInfo()->setAliveStatus(CLONE_ALIVE_STATUS_CHECKPOINT);
//                m_drive->DeleteCookies();
//                finish();
//                return;
//            } else {
//                delay(random(1000, 2000));
//                inputText(WebAPI::getInstance()->tOTP(secretkey.toUtf8().data()).c_str()\
//                      ,ById("approvals_code"));
//                delay(random(500,1000));
//                click(ById("checkpointSubmitButton-actual-button"));
//            }
//        }

//        if(ElementExist(ById("login_error"))) {
//            m_drive->DeleteCookies();
//            finish();
//        }
//    } catch(...) {
//        LOGD << "m_login_email not found";
//    }
}

bool ChromeService::checkProxy(PROXY proxy)
{
    CkHttp http;
    if(proxy.type == AppEnum::E_HTTP_PROXY) {
        http.put_ProxyDomain(proxy.ip.toUtf8().data());
        http.put_ProxyPort(proxy.port);
    } else if(proxy.type == AppEnum::E_SOCKS5_PROXY) {
        http.put_SocksVersion(5);
        http.put_SocksHostname(proxy.ip.toUtf8().data());
        http.put_SocksPort(proxy.port);
        http.put_SocksUsername(proxy.username.toUtf8().data());
        http.put_SocksPassword(proxy.password.toUtf8().data());
    } else {
        return false;
    }
    const char * html = http.quickGetStr("https://www.google.com.vn/");
    if(html) {
        return html;
    } else {
        LOGD << (proxy.ip + ":" + QString::number(proxy.port))  << "proxy died";
        return false;
    }
}

int ChromeService::detectScreen()
{
    QString url = static_cast<webdriverxx::WebDriver*>(m_drive)->GetUrl().c_str();
    if(ElementExist(static_cast<webdriverxx::WebDriver*>(m_drive), webdriverxx::ByXPath("//*[contains(@data-sigil, 'm_login_email')]"))) {
        return AppEnum::E_SCREEN_LOGIN;
    } else if( ElementExist(static_cast<webdriverxx::WebDriver*>(m_drive), webdriverxx::ById("approvals_code"))) {
        return AppEnum::E_SCREEN_ENTER_LOGIN_CODE;
    } else if(ElementExist(static_cast<webdriverxx::WebDriver*>(m_drive), webdriverxx::ByXPath("//*[contains(@value, 'save_device')]")) &&
              ElementExist(static_cast<webdriverxx::WebDriver*>(m_drive), webdriverxx::ByXPath("//*[contains(@value, 'dont_save')]"))) {
       return AppEnum::E_SCREEN_SAVE_BROWSER;
    } else if(ElementExist(static_cast<webdriverxx::WebDriver*>(m_drive), webdriverxx::ByXPath("//*[contains(@href, '/a/nux/wizard/nav.php?step=homescreen_shortcut&skip')]"))) {
        return AppEnum::E_SCREEN_CREATE_SHORTCUT;
    } else if(url.contains("%2Fcheckpoint%2F") ||
              url.contains("282/")) {
        return AppEnum::E_SCREEN_CHECKPOINT;
    } else if(ElementExist(static_cast<webdriverxx::WebDriver*>(m_drive), webdriverxx::ById("m_news_feed_stream"))||
                ElementExist(static_cast<webdriverxx::WebDriver*>(m_drive), webdriverxx::ByXPath("//*[contains(@href, '/profile.php?refid=')]"))) {
       return AppEnum::E_SCREEN_HOME;
    } else {
        return AppEnum::E_SCREEN_UNKNOWN;
    }
}

void ChromeService::onStarted()
{
    LOGD;
    setServiceData(new ServiceData(AppEnum::TYPE_CHROME_SERVICE, m_profileId));
    if(serviceData()->cloneInfo() == nullptr) {
        QDir dir(serviceData()->profilePath());
        dir.removeRecursively();
    }
    startMainProcess();
}

void ChromeService::onMainProcess()
{
    LOGD;
    try {
//        if(serviceData()->getProxy() == nullptr) {
//            // get proxy first
//            getProxy();
//        } else if(serviceData()->cloneInfo() == nullptr) {
//            getClone();
//        } else {
            if(m_drive == nullptr) {
                initChromeDriver();
            } else {
                QString url = static_cast<webdriverxx::WebDriver*>(m_drive)->GetUrl().c_str();

                int screen_id = detectScreen();
                m_screen_stack.append(screen_id);
                LOGD << AppEnum::scrIdStr(screen_id);
                switch (screen_id) {
                default:
                    break;
                }


                // Check screen loop
                foreach(int screen_id, m_screen_stack) {
                    if(m_screen_stack.count(screen_id) > MAX_SCREEN_LOOP) {
                        LOGD << "Screen loop ..... ";
                        finish();
                        break;
                    }
                }
            }
//        }
    } catch(...) {
        handle_eptr(std::current_exception());
    }
}
