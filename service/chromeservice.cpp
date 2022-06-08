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

ChromeService::ChromeService(int profileId, QObject *parent) :
    BaseService(SERVICE_TYPE::TYPE_CHROME_SERVICE,profileId, parent)
{
    m_checkInvLink = false;
    m_getPageList = false;
    LOGD << "--------- ChromeService: " << profileId << " ---------";
}

ChromeService::~ChromeService()
{
//    LOGD;
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
    Chrome chrome;
    JsonObject chromeOptions;
    chrome.SetPlatform(platform::Mac);

    std::vector<std::string> args;
    args.push_back("--user-data-dir=" + serviceData()->profilePath().toStdString());
    args.push_back("--ignore-certificate-errors");
    args.push_back("--proxy-server=" + serviceData()->getProxy()->toString());
    args.push_back("--disable-features=ChromeWhatsNewUI");
#if 0
    if(serviceData()->cloneInfo()->userAgent().isEmpty()) {
        serviceData()->cloneInfo()->setUserAgent(getRandomUserAgent());
    }
#endif

    args.push_back("--disable-notifications");
    args.push_back("--window-position=1500,0");
    chromeOptions.Set<std::vector<std::string>>("args",args);

    std::vector<std::string> switches;
    switches.push_back("enable-automation");
    switches.push_back("load-extension");
    chromeOptions.Set<std::vector<std::string>>("excludeSwitches",switches);



    JsonObject sourceJson = JsonObject();
    sourceJson.Set("intl.accept_languages", "en,en_US");
    sourceJson.Set("profile.password_manager_enabled", false);
    sourceJson.Set("credentials_enable_service", false);
    chromeOptions.Set<picojson::value>("prefs", static_cast<picojson::value>(sourceJson));

#if 0
    JsonObject mobileEmulation = JsonObject();
    mobileEmulation.Set("deviceName", "iPhone 8 Plus");
    chromeOptions.Set<picojson::value>("mobileEmulation",static_cast<picojson::value>(mobileEmulation));
#endif

    chrome.SetChromeOptions(chromeOptions);

    driver = new FDriver(chrome);

    int width = 375;
    int height = 812;

    static int max_width = 0, max_height = 0;
    if(max_width == 0 || max_height == 0) {
        Window window = driver->GetCurrentWindow().Maximize();
        max_width = window.GetSize().width;
        max_height = window.GetSize().height;
    }
    Point point = Point(QRandomGenerator::global()->bounded(max_width - width), 0);;
    driver->GetCurrentWindow().SetPosition(point);
    Size size; size.width = width; size.height = height;
    driver->GetCurrentWindow().SetSize(size);
    driver->Execute("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

    driver->Navigate("https://m.facebook.com");
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
        serviceData()->setProxy(httpProxy);
    }
#endif
}

void ChromeService::getClone()
{
    std::string result = WebAPI::getInstance()->getClone(nullptr, "facebook");
    QJsonObject cloneInfo = QJsonDocument::fromJson(result.c_str()).object().value("cloneInfo").toObject();
    if(!cloneInfo.isEmpty()) {
        LOGD << cloneInfo;
        serviceData()->setCloneInfo(new CloneInfo(cloneInfo));
        if(serviceData()->cloneInfo()) {
            serviceData()->cloneInfo()->setAliveStatus(CLONE_ALIVE_STATUS_STORE);
        }
    }
}

void ChromeService::getActions()
{
    std::string actionsStr = WebAPI::getInstance()->doAction(nullptr, FACEBOOK_APP, serviceData()->cloneInfo()->cloneId().toUtf8().data());
    LOGD << actionsStr.c_str();

    QJsonObject doactionObj = QJsonDocument::fromJson(actionsStr.c_str()).object();
    QString message = doactionObj.value("message").toString();
    bool success = doactionObj.value("success").toBool();
    int code = doactionObj.value("code").toInt();
    if(success && code == 200) {
        serviceData()->setActionsList(doactionObj.value("actions").toArray());
    }
}

void ChromeService::login()
{
    LOGD;
    try {
        QString uid = serviceData()->cloneInfo()->uid();
        QString password = serviceData()->cloneInfo()->password();

        Element element;
        if(FindElement(element, ByXPath("//*[contains(@data-sigil, 'm_login_email')]")) && uid != QString(element.GetAttribute("value").c_str())) {
             inputText(serviceData()->cloneInfo()->uid(),ByXPath("//*[contains(@data-sigil, 'm_login_email')]"));
             delay(random(500, 1000));
        }

        if(FindElement(element, ByXPath("//*[contains(@data-sigil, 'm_login_email')]")) &&
                       password != QString(element.GetAttribute("value").c_str())) {
            inputText(serviceData()->cloneInfo()->password(),ByXPath("//*[contains(@data-sigil, 'password-plain-text-toggle-input')]"));
            delay(random(500, 1000));
        }

        if(ElementExist(ByXPath("//*[contains(@data-sigil, 'touchable login_button_block m_login_button')]"))) {
            click(ByXPath("//*[contains(@data-sigil, 'touchable login_button_block m_login_button')]"));
            delay(5000);
        }

        if(ElementExist(ById("approvals_code"))) {
            QString secretkey = serviceData()->cloneInfo()->secretkey();
            if(secretkey.isEmpty()) {
                serviceData()->cloneInfo()->setAliveStatus(CLONE_ALIVE_STATUS_CHECKPOINT);
                driver->DeleteCookies();
                finish();
                return;
            } else {
                delay(random(1000, 2000));
                inputText(WebAPI::getInstance()->tOTP(secretkey.toUtf8().data()).c_str()\
                      ,ById("approvals_code"));
                delay(random(500,1000));
                click(ById("checkpointSubmitButton-actual-button"));
            }
        }

        if(ElementExist(ById("login_error"))) {
            driver->DeleteCookies();
            finish();
        }
    } catch(...) {
        LOGD << "m_login_email not found";
    }
}

void ChromeService::feedLike(bool acceptLike)
{
    LOGD << "acceptLike: " << acceptLike;
    int operations = QRandomGenerator::global()->bounded(10) + 5;
    for(int i = 0 ; i <operations; i++) {
        Element element;
        if(FindElement(element, ByXPath("/html"))) {
            element.SendKeys(Shortcut() << keys::PageDown);
            delay(1000);
        }

        if(acceptLike && QRandomGenerator::global()->bounded(4) == 1) {
            try {
                std::vector<Element> elements = driver->FindElements(ByXPath("//*[contains(@data-sigil, 'touchable ufi-inline-like like-reaction-flyout')]"));
                foreach(Element element , elements) {
                    if(element.IsDisplayed() && element.GetAttribute("aria-pressed") == "false") {
                        element.Click();
                        delay(2);
                        break;
                    }
                }
            } catch(...) {
                handle_eptr(std::current_exception());
            }
        }

        delayRandom(500, 2000);
    }
}

bool ChromeService::followByPage(QString pageId, AFAction* action)
{
    LOGD;
    QString uid = serviceData()->cloneInfo()->uid();
    QString targetUid = action->fb_id();
    QString fb_dtsg = m_fb_dtsg;
    QString jazoest = m_jazoest;
    QString cookies = getCookies();
    LOGD << "uid: " << uid;
    LOGD << "pageId: " << pageId;
    LOGD << "targetUid: " << targetUid;
    LOGD << "fb_dtsg: " << fb_dtsg;
    LOGD << "jazoest: " << jazoest;
    LOGD << "cookies: " << cookies;


    CkHttp http;
    http.put_ProxyDomain(serviceData()->getProxy()->ip.toUtf8().data());
    http.put_ProxyPort(serviceData()->getProxy()->port);

    CkHttpRequest req;
    req.put_HttpVerb("POST");
    req.put_ContentType("multipart/form-data");

    req.AddHeader("authority", "www.facebook.com");
    req.AddHeader("pragma", "no-cache");
    req.AddHeader("cache-control", "no-cache");
    req.AddHeader("sec-ch-ua", "\" Not;A Brand\";v=\"99\", \"Google Chrome\";v=\"97\", \"Chromium\";v=\"97\"");
    req.AddHeader("accept", "application/json, text/plain, */*");
    req.AddHeader("sec-ch-ua-mobile", "?0");
    req.AddHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.71 Safari/537.36");
    req.AddHeader("viewport-width", "1366");
    req.AddHeader("x-fb-friendly-name", "CometUserFollowMutation");
    req.AddHeader("x-fb-lsd", "G8ufYXRH0qQhgaujpf_KUC");
    req.AddHeader("content-type", "application/x-www-form-urlencoded");
    req.AddHeader("sec-ch-ua-platform", "\"Windows\"");
    req.AddHeader("accept", "*/*");
    req.AddHeader("origin", "https://www.facebook.com");
    req.AddHeader("sec-fetch-site", "same-origin");
    req.AddHeader("sec-fetch-mode", "cors");
    req.AddHeader("sec-fetch-dest", "empty");
    req.AddHeader("referer", "https://www.facebook.com/zuck?_rdr");
    req.AddHeader("accept-language", "en-US,en;q=0.9,vi;q=0.8");
    req.AddHeader("cookie", cookies.toUtf8().data());

    req.AddParam("__a", "1");
    req.AddParam("__dyn", "7AzHxqU5a5Q1ryaxG4VuC0BVU98nwgU76byQdwSwAyU8EW0CEboG4E762S1DwUx609vCxS320om78-221Rwwwg8a8465o-cwfG12wOKdwGwQw9m8wsU9kbxS2218wc61axe3S68f85qfK6E7e58jwGzEaE5e7oqBwJK2W5olwUwgojUlDw-wAxe1MBx_y88E6a1PwyBwJwSyES0Io88cA0z8");
    req.AddParam("__csr", "gtgqhsBNz6MBbA99tTln99j8GSGW_kCAIOF9tLFLdLGiBK-AQGt5ALEhrF6QQmQUyqGa_H9QFmyk8-XKirihsHy6Fp99AGyoF4-EKKFpQazkq5oCt6LAz4EnuaBx26VoCmfFohJaqdHWyrCKVpF9GxqmlRxybBwJAGax16BK6K324VpUsVAcwDyo8p8DCwPzEC217xidw-x7CK6oK2yeBAG1mxO8Cwgoigjx-fyXzUtK7pUCt0EGdAgeEcUbEdodU9Ujxq4A3y1cwoEy9z9UCdwKwn98kw9G3qiEO16x20i62i4UK06e-7o0kuwpAawvU0gmgmwOwfRVo0QC3-2y01Dcwdlw1qgm9hOmywuoDAgdszykcy85-7Q1nw0xNwgkEmxG2G0hG016gP0");
    req.AddParam("__req", "i");
    req.AddParam("__hs", "19007.HYP:comet_pkg.2.1.0.2.");
    req.AddParam("dpr", "1");
    req.AddParam("__ccg", "GOOD");
    req.AddParam("__rev", "1004944189");
    req.AddParam("__s", "bckcyr:ez3pnn:78obm6");
    req.AddParam("__hsi", "7053415870342166096-0");
    req.AddParam("__comet_req", "1");
    req.AddParam("fb_dtsg", fb_dtsg.toUtf8().data());
    req.AddParam("jazoest", jazoest.toUtf8().data());
    req.AddParam("lsd", "G8ufYXRH0qQhgaujpf_KUC");
    req.AddParam("__spin_r", "1004944189");
    req.AddParam("__spin_b", "trunk");
    req.AddParam("__spin_t", "1642251357");
    req.AddParam("fb_api_caller_class", "RelayModern");
    req.AddParam("fb_api_req_friendly_name", "CometUserFollowMutation");
    req.AddParam("variables", QString("{\"input\":{\"subscribe_location\":\"PROFILE\",\"subscribee_id\":\"%1\",\"actor_id\":\"%2\",\"client_mutation_id\":\"2\"},\"scale\":1}").arg(targetUid).arg(pageId).toUtf8().data());
    req.AddParam("server_timestamps", "true");
    req.AddParam("doc_id", "4184140341672266");
    req.AddParam("fb_api_analytics_tags", "[\"qpl_active_flow_ids=30605361\"]");

    QString variable = QString("{\"input\":{\"subscribe_location\":\"PROFILE\",\"subscribee_id\":\"%1\",\"actor_id\":\"%2\",\"client_mutation_id\":\"0\"},\"scale\":1.5}").arg(targetUid).arg(pageId);
    req.AddParam("variables", variable.toUtf8().data());

    CkHttpResponse *resp = http.PostUrlEncoded("https://www.facebook.com/api/graphql",req);
    if (http.get_LastMethodSuccess() == false) {
        LOGD << "error: " << http.lastErrorText();
    } else {
         QJsonObject respObj = QJsonDocument::fromJson(resp->bodyStr()).object();
//         LOGD << "body: " << resp->bodyStr();
         if(!respObj.isEmpty() && !respObj.contains("errors")) {
             LOGD << "Follow success";
             return true;
         } else {
             LOGD << "Follow failed";
         }
    }
    return false;
}

bool ChromeService::getPagesOfUid()
{
    LOGD;
    QString fb_dtsg = m_fb_dtsg;
    QString cookies = getCookies();

    CkHttp http;
    CkHttpRequest req;
    req.put_HttpVerb("POST");
    req.put_Path("/api/graphql");
    req.put_ContentType("multipart/form-data;charset=utf-8");

    req.AddHeader("authority", "www.facebook.com");
    req.AddHeader("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98\"");
    req.AddHeader("accept", "application/json, text/plain, */*");
    req.AddHeader("sec-ch-ua-mobile", "?0");
    req.AddHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36");
    req.AddHeader("sec-ch-ua-platform", "\"Windows\"");
    req.AddHeader("origin", "https://www.facebook.com");
    req.AddHeader("sec-fetch-site", "none");
    req.AddHeader("sec-fetch-mode", "navigate");
    req.AddHeader("sec-fetch-dest", "document");
    req.AddHeader("accept-language", "en-US,en;q=0.9,vi;q=0.8");
    req.AddHeader("cookie", cookies.toUtf8().data());

    req.AddParam("fb_dtsg", fb_dtsg.toUtf8().data());
    req.AddParam("fb_api_caller_class", "RelayModern");
    req.AddParam("fb_api_req_friendly_name", "PageCometLaunchpointPagesListQuery");
    req.AddParam("variables", "{\"scale\":1.5}");
    req.AddParam("doc_id", "4700711393323643");


    CkHttpResponse *resp = http.PostUrlEncoded("https://www.facebook.com/api/graphql",req);
    if (http.get_LastMethodSuccess() == false) {
        LOGD << "error: " << http.lastErrorText();
    } else {
        QJsonObject respObj = QJsonDocument::fromJson(resp->bodyStr()).object();
        QJsonArray pages = respObj.value("data").toObject().value("viewer").toObject().value("admined_pages").toObject().value("nodes").toArray();

        LOGD << "pages: " << pages;
        QStringList pageList;
        for(int i = 0 ; i < pages.size(); i++) {
            QJsonObject obj = pages.at(i).toObject();
            if(!obj.value("id").toString().isEmpty())
                pageList.append(obj.value("id").toString());
        }
        serviceData()->cloneInfo()->setPageList(pageList);
        return true;
    }
    return false;
}

bool ChromeService::getFb_dtsg()
{
    try {
//        std::vector<Element> fb_dtsgs = driver->FindElements(ByName("fb_dtsg"));
//        if(fb_dtsgs.size() > 0) {
//            m_fb_dtsg = fb_dtsgs[0].GetAttribute("value").c_str();
//        }

//        std::vector<Element> jazoests = driver->FindElements(ByName("jazoest"));
//        if(jazoests.size() > 0) {
//            m_jazoest = jazoests[0].GetAttribute("value").c_str();
//        }

//        std::string source = driver->Get("https://m.facebook.com/composer/ocelot/async_loader/?publisher=feed&hc_location=ufi").GetSource();
//        std::string fb_dtsg;
//        std::string regx = R"(name=\\\"fb_dtsg\\\" value=\\\"([\s\S]*?)\\\")";
//        std::smatch matches;
//        if (std::regex_search(source, matches, std::regex(regx)))
//        {
//            fb_dtsg = matches[1];
//            m_fb_dtsg = fb_dtsg.c_str();
//        }
//        driver->Back();

//        if(!fb_dtsg.empty()) {
//            return true;
//        }

        CkHttp http;
        CkHttpRequest req;
        req.put_HttpVerb("POST");
        req.put_ContentType("multipart/form-data");

        http.SetRequestHeader("authority", "mbasic.facebook.com");
        http.SetRequestHeader("pragma", "no-cache");
        http.SetRequestHeader("cache-control", "no-cache");
        http.SetRequestHeader("sec-ch-ua", "\" Not;A Brand\";v=\"99\", \"Google Chrome\";v=\"97\", \"Chromium\";v=\"97\"");
        http.SetRequestHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        http.SetRequestHeader("sec-ch-ua-mobile", "?0");
        http.SetRequestHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.71 Safari/537.36");
        http.SetRequestHeader("sec-ch-ua-platform", "\"Windows\"");
        http.SetRequestHeader("upgrade-insecure-requests", "1");
        http.SetRequestHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        http.SetRequestHeader("origin", "https://www.facebook.com");
        http.SetRequestHeader("sec-fetch-site", "none");
        http.SetRequestHeader("sec-fetch-mode", "navigate");
        http.SetRequestHeader("sec-fetch-user", "?1");
        http.SetRequestHeader("sec-fetch-dest", "document");
        http.SetRequestHeader("accept-language", "vi-VN,vi;q=0.9,fr-FR;q=0.8,fr;q=0.7,en-US;q=0.6,en;q=0.5");
        http.SetRequestHeader("cookie", getCookies().toUtf8().data());

        const char *resp = http.quickGetStr("https://mbasic.facebook.com/messages/");
        if (http.get_LastMethodSuccess() == false) {
            LOGD << "error: " << http.lastErrorText();
        } else {
//            LOGD << resp;
            std::string source = std::string(resp);
            std::string fb_dtsg, jazoests;
            std::string regx = R"(name=\"fb_dtsg\" value=\"([\s\S]*?)\")";
            std::smatch matches;
            if (std::regex_search(source, matches, std::regex(regx)))
            {
                fb_dtsg = matches[1];
                m_fb_dtsg = fb_dtsg.c_str();
            }

            if (std::regex_search(source, matches, std::regex(R"(name=\"jazoest\" value=\"([\s\S]*?)\")")))
            {
                jazoests = matches[1];
                m_jazoest = jazoests.c_str();
            }
        }

    } catch(...) {
        handle_eptr(std::current_exception());
    }

    return false;
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

bool ChromeService::getInviteLink(QJsonObject& data, QString uid)
{
    CkHttp http;
    QString url = QString("https://api.pagesub.me/public-api/v1/clone/get-invite?uid=%1").arg(uid);
    const char * html = http.quickGetStr(url.toUtf8().data());
    if(html) {
        QJsonObject raw =  QJsonDocument::fromJson(html).object();
        LOGD << "raw: " << raw;
        data = raw.value("data").toObject();
        return true;
    } else {
        LOGD << "Api error";
    }
    return false;
}

bool ChromeService::acceptInvitation(QJsonObject &data)
{
    LOGD;
    QString link = data.value("invite_link").toString();
    if(!link.isEmpty()) {
        try {
            driver->Navigate(link.toStdString());
            QString url = driver->GetUrl().c_str();
            QString first_name = "Clone";
            QString last_name = serviceData()->cloneInfo()->uid();
            QString invitation_token = url.mid(url.indexOf("?token=") + 7);
            QString UID = serviceData()->cloneInfo()->uid();
            QString PASSWORD = serviceData()->cloneInfo()->password();
            QString fb_dtsg = m_fb_dtsg;
            QString COOKIE = getCookies();

            LOGD << "first_name: " << first_name << "\n" \
                    << "last_name: " << last_name << "\n" \
                    << "invitation_token: " << invitation_token << "\n" \
                    << "UID: " << UID << "\n" \
                    << "PASSWORD: " << PASSWORD << "\n" \
                    << "fb_dtsg: " << fb_dtsg << "\n" \
                    << "COOKIE: " << COOKIE << "\n";
            CkHttp http;
            CkHttpRequest req;

            req.put_HttpVerb("POST");
            req.put_ContentType("multipart/form-data;charset=utf-8");

            req.AddParam("first_name", first_name.toUtf8().data());
            req.AddParam("last_name", last_name.toUtf8().data());
            req.AddParam("invitation_token", invitation_token.toUtf8().data());
            req.AddParam("__user", UID.toUtf8().data());
            req.AddParam("fb_dtsg", fb_dtsg.toUtf8().data());
            req.AddParam("ajax_password", PASSWORD.toUtf8().data());
            req.AddParam("confirmed", "1");
            req.AddParam("jazoest", "21986");
            req.AddParam("lsd", "D_YX-qPTxm83MziqgRce71");
            req.AddParam("__spin_r", "1005145724");
            req.AddParam("__spin_b", "trunk");
            req.AddParam("__spin_t", "1646372818");
            req.AddParam("__jssesw", "1");

            req.AddHeader("authority", "www.facebook.com");
            req.AddHeader("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98\"");
            req.AddHeader("accept", "application/json, text/plain, */*");
            req.AddHeader("sec-ch-ua-mobile", "?0");
            req.AddHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36");
            req.AddHeader("sec-ch-ua-platform", "\"Windows\"");
            req.AddHeader("origin", "https://www.facebook.com");
            req.AddHeader("sec-fetch-site", "none");
            req.AddHeader("sec-fetch-mode", "navigate");
            req.AddHeader("sec-fetch-dest", "document");
            req.AddHeader("accept-language", "en-US,en;q=0.9,vi;q=0.8");
            req.AddHeader("Cookie", COOKIE.toUtf8().data());

            CkHttpResponse *resp = http.PostUrlEncoded("https://business.facebook.com/business/invitation/login/",req);
            if (http.get_LastMethodSuccess() == false) {

            } else {
                if(QString(resp->bodyStr()).isEmpty()) {
                    driver->Back();
                    return true;
                }
            }
            driver->Back();
        } catch(...) {
            handle_eptr(std::current_exception());
        }
    } else {
        LOGD << "No invite link";
    }
    return false;
}



bool ChromeService::submitAcceptedInvitation(QJsonObject data)
{
    LOGD;
    data["status"] = "accepted";

    CkHttp http;
    http.put_ConnectTimeout(30);
    http.put_ReadTimeout(30);
    http.SetRequestHeader("Content-Type", "application/json");

    CkHttpResponse *resp = http.PostJson("https://api.pagesub.me/public-api/v1/clone/update-invite", QJsonDocument(data).toJson(QJsonDocument::Compact).data());
    if (!http.get_LastMethodSuccess()) {
        LOGD << http.lastErrorText();
        return false;
    } else {
        QJsonObject data = QJsonDocument::fromJson(resp->bodyStr()).object();
        return data.value("data").toBool(false);
    }
}

int ChromeService::detectScreen()
{
    QString url = driver->GetUrl().c_str();
    if(ElementExist(ByXPath("//*[contains(@data-sigil, 'm_login_email')]"))) {
        return AppEnum::E_SCREEN_LOGIN;
    } else if( ElementExist(ById("approvals_code"))) {
        return AppEnum::E_SCREEN_ENTER_LOGIN_CODE;
    } else if(ElementExist(ByXPath("//*[contains(@value, 'save_device')]")) &&
              ElementExist(ByXPath("//*[contains(@value, 'dont_save')]"))) {
       return AppEnum::E_SCREEN_SAVE_BROWSER;
    } else if(ElementExist(ByXPath("//*[contains(@href, '/a/nux/wizard/nav.php?step=homescreen_shortcut&skip')]"))) {
        return AppEnum::E_SCREEN_CREATE_SHORTCUT;
    } else if(url.contains("%2Fcheckpoint%2F") ||
              url.contains("282/")) {
        return AppEnum::E_SCREEN_CHECKPOINT;
    } else if(ElementExist(ById("m_news_feed_stream"))||
                ElementExist(ByXPath("//*[contains(@href, '/profile.php?refid=')]"))) {
       return AppEnum::E_SCREEN_HOME;
    } else {
        return AppEnum::E_SCREEN_UNKNOWN;
    }
}

void ChromeService::onStarted()
{
    LOGD;
    setServiceData(new ServiceData(BaseService::TYPE_CHROME_SERVICE, m_profileId));
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
        if(serviceData()->getProxy() == nullptr) {
            // get proxy first
            getProxy();
        } else if(serviceData()->cloneInfo() == nullptr) {
            getClone();
        } else {
            if(driver == Q_NULLPTR) {
                initChromeDriver();
            } else {
                QString url = driver->GetUrl().c_str();

                int screen_id = detectScreen();
                m_screen_stack.append(screen_id);
                LOGD << AppEnum::scrIdStr(screen_id);
                switch (screen_id) {
                case AppEnum::AppEnum::E_SCREEN_LOGIN: {
                    QString uid = serviceData()->cloneInfo()->uid();
                    QString password = serviceData()->cloneInfo()->password();
                    Element element;
                    if(FindElement(element, ByXPath("//*[contains(@data-sigil, 'm_login_email')]")) && uid != QString(element.GetAttribute("value").c_str())) {
                         inputText(serviceData()->cloneInfo()->uid(),ByXPath("//*[contains(@data-sigil, 'm_login_email')]"));
                         delay(random(500, 1000));
                    }

                    if(FindElement(element, ByXPath("//*[contains(@data-sigil, 'm_login_email')]")) &&
                                   password != QString(element.GetAttribute("value").c_str())) {
                        inputText(serviceData()->cloneInfo()->password(),ByXPath("//*[contains(@data-sigil, 'password-plain-text-toggle-input')]"));
                        delay(random(500, 1000));
                    }

                    if(ElementExist(ByXPath("//*[contains(@data-sigil, 'touchable login_button_block m_login_button')]"))) {
                        click(ByXPath("//*[contains(@data-sigil, 'touchable login_button_block m_login_button')]"));
                        delay(5000);
                    }
                }
                    break;
                case AppEnum::E_SCREEN_ENTER_LOGIN_CODE:
                    if(ElementExist(ById("approvals_code"))) {
                        QString secretkey = serviceData()->cloneInfo()->secretkey();
                        if(secretkey.isEmpty()) {
                            serviceData()->cloneInfo()->setAliveStatus(CLONE_ALIVE_STATUS_CHECKPOINT);
                            driver->DeleteCookies();
                            finish();
                            return;
                        } else {
                            delay(random(1000, 2000));
                            inputText(WebAPI::getInstance()->tOTP(secretkey.toUtf8().data()).c_str()\
                                  ,ById("approvals_code"));
                            delay(random(500,1000));
                            click(ById("checkpointSubmitButton-actual-button"));
                        }
                    }
                    break;
                case AppEnum::E_SCREEN_SAVE_BROWSER:
                    click(ByName("submit[Continue]"));
                    break;
                case AppEnum::E_SCREEN_CREATE_SHORTCUT:
                    click(ByXPath("//*[contains(@href, '/a/nux/wizard/nav.php?step=homescreen_shortcut&skip')]"));
                    break;
                case AppEnum::E_SCREEN_CHECKPOINT:
                    serviceData()->cloneInfo()->setAliveStatus(CLONE_ALIVE_STATUS_CHECKPOINT);
                    driver->DeleteCookies();
                    finish();
                    break;
                case AppEnum::E_SCREEN_HOME:
                    serviceData()->cloneInfo()->setAliveStatus(CLONE_ALIVE_STATUS_STORE);

#if 0
                    if(serviceData()->getActionList() == nullptr) {
                        getActions();
                    } else {
                        if(serviceData()->getActionList()->length() == 0) {
                            LOGD << "Mission complete ...";
                            finish();
                        } else {
                        AFAction* action = serviceData()->getRandomAction();
                            if(action) {
                                switch (action->action_type()) {
                                case AFAction::E_FACEBOOK_ACTION_FEED:
                                    feedLike(false);
                                    break;
                                case AFAction::E_FACEBOOK_ACTION_FEEDLIKE:
                                    feedLike(true);
                                    break;
                                case AFAction::E_FACEBOOK_ACTION_PAGESUB:
                                    followByPage("xxxxx", action);
                                    break;
                                default:
                                    break;
                                }
                                delete action;
                            }
                        }
                    }
#else

                    LOGD << "m_checkInvLink: " << m_checkInvLink;
                    LOGD << "m_getPageList: " << m_getPageList;
                    if(!m_checkInvLink) {
                        if(m_fb_dtsg.isEmpty()) {
                            getFb_dtsg();
                            delayRandom(1000, 2000);
                        } else {
                            QJsonObject data;
                            m_checkInvLink = getInviteLink(data, serviceData()->cloneInfo()->uid());
                            if(data.contains("invite_link")) {
                                if(acceptInvitation(data)) {
                                    submitAcceptedInvitation(data);
                                }
                                delayRandom(2000, 4000);
                            }
                        }
                    }  else if(!m_getPageList){
                        // get pages
                        getPagesOfUid();
                        m_getPageList = true;
                        delayRandom(2000, 4000);
                    } else {
                        LOGD << "page list: " << serviceData()->cloneInfo()->pageList();
                        for(int i = 0; i < serviceData()->cloneInfo()->pageList().size(); i++) {
                            LOGD << "follow by page: " << serviceData()->cloneInfo()->pageList().at(i);
                            QJsonObject action;
                            action["service_code"] = "XXXX";
                            action["fb_id"] = "100006710623291";
                            action["count"] = 100;
                            action["action"] = "PageSub";
                            if(!followByPage(serviceData()->cloneInfo()->pageList().at(i), new AFAction(action))) {
                                break;
                            } else {
                            delayRandom(4000, 7000);
                            }
                        }
                        finish();
                    }
#endif
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
