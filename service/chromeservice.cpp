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
#include <api/dbpapi.h>
#include <webdriverxx.h>
#include <QDateTime>
#include <api/dropboxapi.h>
#include <QRandomGenerator64>
#include <QRandomGenerator>

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

QString getRandomCaption() {
  QStringList captionList =   QStringList()
          << "Hi everyone!"
          << "Are you ready ...?"
          << "Love you <3"
          << "Boom boom..."
          << "Good morning!"
          << "Good afternoon~"
          << "Yeah yeah."
          << "Being happy :))"
          << "So sad ..."
          << "Tell me something .."
          << "Say something???"
          << "Say oh yeah :v"
          << "It's ok????"
          << "Good?"
          << "Let me see your feeling? :)))))))))))"
          << "Don't leave me :(("
          << "Love me????"
          << "Just look in my eyes? :D"
          << "Wowwwwwwwwwwwwwww ...."
          << "Oh my god"
          << "Beautiful girl?"
          << "Cute or something????"
          << "Don't leave me alone..."
          << "Feeling sad ..."
          << "So deep :)))))"
          << "Yessss .... again... yes..."
          << "Why? tell me why?..."
          << "Tried my best~"
          << "Don't let me say bad words ..."
          << "Broken heart ... :((";

  return captionList.at(QRandomGenerator::global()->bounded(captionList.length()));
}

QString getRandomComment() {
    QStringList iconList = QStringList() \
    << "(y)	"
     << ">:("
     << ">:-("
     << "O:)"
     << "O:-)"
     << ":-P" << ":P"  << ":-p" << ":p" << "=P"
     << "3:)" << "3:-)"
     << ":putnam:"
     << "<3"
     << ">:O" << ">:-O" << ">:o" << ">:-o"
     << ":-(" << ":(" << ":[" << "=("
     << ":/" << ":-/" << ":\\"
     << "^_^"
     << ":|]"
     << "o.O" << "O.o"
     << "8-|" << "8|" << "B-|" << "B|"
     << ":-O" << ":O" << ":-o" << ":o"
     << "3:)" << "3:-)"
     << ":-*" << ":*"
     << "(^^^)"
     << ":’("
     << "-_-"
     << "8-)" << "8)" << "B-)" << "B)"
     << ":-(" << ":(" << ":[" << "=("
     << ":v"
     << ":-)" << ":)" << ":]" << "=)"
     << ":3"
     << ">.<"
     << ":-D" << ":D" << "=D"
     << ":poop:"
     << "<(\")"
     << "T_T";

    QStringList textList = QStringList() << "wow .. " << "hi " << "good " << "fun " << "great! " << "haha .. " << "lol" << "be angry.. " << "hot " << "cold " << "look here " << "hummmmm " << "humm " << "noooo " << "oh " << "my god " << "sad " << "good job..";

    return (QRandomGenerator::global()->bounded(2) == 1? textList.at(QRandomGenerator::global()->bounded(textList.size())) : "") +
            (QRandomGenerator::global()->bounded(2) == 1? (" ") + iconList.at(QRandomGenerator::global()->bounded(iconList.size())) : "") +
            (QRandomGenerator::global()->bounded(2) == 1? (" ") + iconList.at(QRandomGenerator::global()->bounded(iconList.size())) : "") +
            (QRandomGenerator::global()->bounded(2) == 1? (" ") + iconList.at(QRandomGenerator::global()->bounded(iconList.size())) : "") +
            (QRandomGenerator::global()->bounded(2) == 1? (" ") + iconList.at(QRandomGenerator::global()->bounded(iconList.size())) : "") +
            (QRandomGenerator::global()->bounded(2) == 1? (" ") + iconList.at(QRandomGenerator::global()->bounded(iconList.size())) : "");
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
        LOGD << by.GetStrategy().c_str() << ":" << by.GetValue().c_str() << " not found";
        return false;
    }
}

bool FindAndClickElement(webdriverxx::WebDriver* driver, webdriverxx::Element &element, const webdriverxx::By &by)
{
    try {
        element = driver->FindElement(by);
        element.Click();
        return true;
    } catch(...) {
        handle_eptr(std::current_exception());
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
//    args.push_back("--start-maximized");
//    args.push_back("--start-fullscreen");
    args.push_back("--single-process");
    args.push_back("--disable-dev-shm-usage");
//    args.push_back("--incognito");
    args.push_back("--disable-blink-features=AutomationControlled");
    args.push_back("disable-infobars");

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
    sourceJson.Set("profile.password_manager_enabled", false);
    sourceJson.Set("credentials_enable_service", false);
    sourceJson.Set("profile", JsonObject().Set("exit_type", "Normal"));
    chromeOptions.SetPrefs(sourceJson);

    chromeOptions.SetBinary("/usr/bin/google-chrome");

#if 0
    webdriverxx::chrome::MobileEmulation mobileEmulation;
    mobileEmulation.SetdeviceName("iPhone X");
//    mobileEmulation.SetuserAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 13_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.2 Mobile/15E148 Safari/604.1");
//    mobileEmulation.SetdeviceMetrics(webdriverxx::chrome::device::deviceMetrics().Settouch(true).Setwidth(375).Setheight(812).SetpixelRatio(3));
    chromeOptions.SetMobileEmulation(mobileEmulation);
#endif

    chrome.SetChromeOptions(chromeOptions);
    chrome.SetPath(QString(QDir::currentPath() + "/chromedriver").toStdString());

    const char * url ="http://localhost:9515/";
    m_drive = new WebDriver(chrome, webdriverxx::Capabilities(),  url);

    static_cast<webdriverxx::WebDriver*>(m_drive)->SetTimeoutMs(timeout::Implicit, 10000);
    static_cast<webdriverxx::WebDriver*>(m_drive)->SetTimeoutMs(timeout::PageLoad, 30000);
    static_cast<webdriverxx::WebDriver*>(m_drive)->SetTimeoutMs(timeout::Script, 1000);

    static_cast<webdriverxx::WebDriver*>(m_drive)->Execute("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

    JsonObject params;
    params.Set("source", "Object.defineProperty(navigator, 'webdriver', { get: () => undefined })");
    static_cast<webdriverxx::WebDriver*>(m_drive)->ExecuteCdpCommand("Page.addScriptToEvaluateOnNewDocument", params);

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
    QJsonObject retval = DBPApi::instance()->getClone();
    if(retval["success"].toBool()) {
        QJsonObject cloneinfo = retval["clone_info"].toObject();
        if(!cloneinfo.isEmpty()) {
            serviceData()->setCloneInfo(new CloneInfo(cloneinfo));
            if(serviceData()->cloneInfo()) {
                serviceData()->cloneInfo()->setStatus(CLONE_ALIVE_STATUS_STORE);
            }
        }
    }
}


void ChromeService::login()
{
    LOGD;
//    static_cast<webdriverxx::WebDriver*>(m_drive)->Navigate("https://www.tiktok.com/login/phone-or-email/email");
    Element element;
//    bool enter_username, enter_password;
    for(int i = 0; i < 20; i ++) {
//        if(FindAndClickElement(static_cast<webdriverxx::WebDriver*>(m_drive), element, webdriverxx::ByXPath("//button[@data-e2e='top-login-button' and text()='Log in']"))) {
//            delay(1000);
//        } else {
//            LOGD << "Could not click login button";
//        }

////        //*[text()='Use phone / email / username']
//        if(FindAndClickElement(static_cast<webdriverxx::WebDriver*>(m_drive), element, webdriverxx::ByXPath("//*[text()='Use phone / email / username']"))) {
//            delay(1000);
//        } else {
//            LOGD << "Could not click phone or email button";
//            if(ElementExist(static_cast<webdriverxx::WebDriver*>(m_drive), webdriverxx::ByXPath("//*[@data-e2e='login-frame']"))) {
//                std::vector<Element>  iframes= static_cast<webdriverxx::WebDriver*>(m_drive)->FindElements(ByTag("iframe"));
//                if(iframes.size() > 0) {
//                    static_cast<webdriverxx::WebDriver*>(m_drive)->SetFocusToFrame(iframes[0]);
//                }
//            }
//        }

//        if(FindAndClickElement(static_cast<webdriverxx::WebDriver*>(m_drive), element, webdriverxx::ByXPath("//*[text()='Log in with email or username']"))) {
//            delay(2000);
//        } else {
//            LOGD << "Could not click 'Log in with email or username' button";
//        }

        //Log in with email or username

//        if(FindElement(static_cast<webdriverxx::WebDriver*>(m_drive), element, webdriverxx::ByXPath("//input[@placeholder='Email or username']"))) {
//            if(!enter_username) {
//                QString username = serviceData()->cloneInfo()->username();
//                element.Click();
//                element.Clear();
//                for(int i = 0; i < username.length(); i++) {
//                    element.SendKeys(QString(username.at(i)).toStdString());
//                    delayRandom(100,300);
//                }
//                enter_username = true;
//                delay(2000);
//            }
//        }

//        if(FindElement(static_cast<webdriverxx::WebDriver*>(m_drive), element, webdriverxx::ByXPath("//input[@placeholder='Password']"))) {
//            if(!enter_password) {
//                element.Click();
//                element.Clear();
//                QString password = serviceData()->cloneInfo()->password();
//                for(int i = 0; i < password.length(); i++) {
//                    element.SendKeys(QString(password.at(i)).toStdString());
//                    delayRandom(100,300);
//                }
//                delay(2000);
//                enter_password = true;
//            }
//        }

//        if(FindElement(static_cast<webdriverxx::WebDriver*>(m_drive), element, webdriverxx::ByXPath("//button[text()='Log in']"))) {
//            element.Submit();
//            delay(3000);
//        }

        //*[@data-e2e='profile-icon']
        if(FindElement(static_cast<webdriverxx::WebDriver*>(m_drive), element, webdriverxx::ByXPath("//*[@data-e2e='profile-icon']"))) {
            break;
        }
        delayRandom(2000, 3000);
    }
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
    QString url = static_cast<webdriverxx::WebDriver*>(m_drive)->GetUrl().c_str(); //nav-following
    if(ElementExist(static_cast<webdriverxx::WebDriver*>(m_drive), webdriverxx::ByXPath("//*[contains(@data-e2e, 'nav-foryou')]")) &&
                ElementExist(static_cast<webdriverxx::WebDriver*>(m_drive), webdriverxx::ByXPath("//*[contains(@data-e2e, 'nav-following')]"))) {
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
        if(serviceData()->cloneInfo() == nullptr) {
            getClone();
        } else {
            if(m_drive == nullptr) {
                initChromeDriver();
            } else {
                QString url = static_cast<webdriverxx::WebDriver*>(m_drive)->GetUrl().c_str();

                int screen_id = detectScreen();
                m_screen_stack.append(screen_id);
                LOGD << AppEnum::scrIdStr(screen_id);
                switch (screen_id) {
                case AppEnum::E_SCREEN_HOME: {
                    if(ElementExist(static_cast<webdriverxx::WebDriver*>(m_drive), webdriverxx::ByXPath("//button[@data-e2e='top-login-button' and text()='Log in']"))) {
                        login();
                    } else {
                        feed();
                        long lastUploadTime = serviceData()->cloneInfo()->lastUploadTime();
                        qint64 currentTime = QDateTime::currentMSecsSinceEpoch ();
                        if(currentTime - lastUploadTime > (6 * 60 * 60 * 1000)) {
                            uploadNewVideo();
                        }
                        finish();
                    }
                }
                    break;
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
        }
    } catch(...) {
        handle_eptr(std::current_exception());
    }
}

void ChromeService::feed() {
    bool doComment = false;
    int operations = QRandomGenerator::global()->bounded(20) + 40;
    for(int i = 0 ; i <operations; i++) {
        LOGD << "Feeding ...";
        Element element;
        if(FindElement(static_cast<webdriverxx::WebDriver*>(m_drive), element, ByXPath("/html"))) {
            element.SendKeys(Shortcut() << keys::PageDown);
            delay(1000);
        }

        if(QRandomGenerator::global()->bounded(20) == 1) {
            try {
                std::vector<Element> elements = static_cast<webdriverxx::WebDriver*>(m_drive)->FindElements(ByXPath("//button[@type='button']"));
                foreach(Element element , elements) {
                    try {
                        element.FindElement(ByXPath("//*[@data-e2e='like-icon']"));
                        element.Click();
                        break;
                    } catch (...) {}
                }
            } catch(...) {
                handle_eptr(std::current_exception());
            }
        }


        // Do comment
        if(doComment) {
            Element element;
            if(FindElement(static_cast<webdriverxx::WebDriver*>(m_drive), element, ByXPath("//*[@data-e2e='comment-input']"))) {
                try {
                    element = element.FindElement(ByXPath("//*[@data-text='true']"));
                    element.SendKeys(getRandomComment().toStdString());
                    delay(1000);
                    FindAndClickElement(static_cast<webdriverxx::WebDriver*>(m_drive), element, ByXPath("//*[@data-e2e='comment-post' and text()='Post']"));
                    delay(2000);
                    static_cast<webdriverxx::WebDriver*>(m_drive)->Back();
                    doComment = false;
                } catch (...) {
                    handle_eptr(std::current_exception());
                }
            }
        } else {
            if(QRandomGenerator::global()->bounded(20) == 1) {
                try {
                    std::vector<Element> elements = static_cast<webdriverxx::WebDriver*>(m_drive)->FindElements(ByXPath("//button[@type='button' and contains(@class,'ButtonActionItem')]"));
                    foreach(Element element , elements) {
                        try {
                            element.FindElement(ByXPath("//*[@data-e2e='comment-icon']"));
                            element.Click();
                            doComment = true;
                            break;
                        } catch (...) {}
                    }
                } catch(...) {
                    handle_eptr(std::current_exception());
                }
            }
        }

        delayRandom(5000, 20000);
    }
}

void ChromeService::uploadNewVideo() {
    LOGD;
    QJsonObject retval = DBPApi::instance()->getVideoPath(serviceData()->cloneInfo()->clonedFrome());
    if(retval["success"].toBool()) {
        QJsonObject video_info = retval["video_info"].toObject();
        QString video_id = video_info["video_id"].toString();
        QString video_path = video_info["video_path"].toString();
        LOGD << video_id << "|" << video_path;
        QString local_path = QString(QDir::currentPath() + "/video.mp4").toUtf8();
        if(DropboxAPI::instance()->downloadFile(video_path.toUtf8().data(), local_path.toUtf8().data())) {
            static_cast<webdriverxx::WebDriver*>(m_drive)->Navigate("https://www.tiktok.com/upload?lang=en");


            for(int i = 0; i < 30; i++) {
                if(ElementExist(static_cast<webdriverxx::WebDriver*>(m_drive), webdriverxx::ByXPath("//*[contains(., 'Upload video')]"))) {
                   std::vector<Element> iframes = static_cast<webdriverxx::WebDriver*>(m_drive)->FindElements(ByTag("iframe"));
                   if(iframes.size()) {
                       static_cast<webdriverxx::WebDriver*>(m_drive)->SetFocusToFrame(iframes[0]);
                   }

                   webdriverxx::Element element;
                   //Step1: Turn on copyright checker first
                   if(FindElement(static_cast<webdriverxx::WebDriver*>(m_drive), element,ByClass("tiktok-switch"))) {
                       if(element.GetAttribute("aria-checked") == "false")
                            element.Click();
                       else {

                           // Step2: input caption
                           if(FindElement(static_cast<webdriverxx::WebDriver*>(m_drive), element,ByXPath("//*[@data-text='true']"))) {
                              if(element.GetText().empty()) {
                                  QString caption = getRandomCaption() + " #fyp #foryou #cutegirl #cutebaby #xuhuong #trending .";
                                  element.SendKeys(caption.toStdString());
                              } else {
                                  // Step3: input file
                                  if(FindElement(static_cast<webdriverxx::WebDriver*>(m_drive), element,ByXPath("//input[@type='file']"))) {
                                     element.SendKeys(local_path.toStdString());
                                     delay(15000);
                                  }
                              }
                           }

//                           // Step 4: Waiting for no issues
                           if(FindElement(static_cast<webdriverxx::WebDriver*>(m_drive), element,ByXPath("//*[contains(text(), 'No issues detected.')]"))) {

                               // Step 5: click post
                              if(FindElement(static_cast<webdriverxx::WebDriver*>(m_drive), element,ByXPath("//*[contains(@class, 'btn-post')]"))) {
                                 try {
                                     element = element.FindElement(ByTag("button"));
                                     element.Click();
                                 } catch(...) {}
                              }
                            }
                       }
                   }

                  // Step 5: submit when video is uploaded
                  if(FindElement(static_cast<webdriverxx::WebDriver*>(m_drive), element,ByXPath("//*[contains(text(), 'Your video is being uploaded to TikTok!')]"))) {
                     LOGD << "Upload success!";
                     DBPApi::instance()->updateVideoStatus(video_id, "used");
                     qint64 currentTime = QDateTime::currentMSecsSinceEpoch ();
                     serviceData()->cloneInfo()->setLastUploadTime(currentTime);
                     break;
                  }
                }

                delayRandom(1000, 2000);
            }
        }
    }
}
