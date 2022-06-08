#include <QCoreApplication>
#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QQmlContext>
//#include <appmain.h>
//#include <appmodel.h>
#include <CkGlobal.h>
//#include "log.h"
#include <QDebug>

#include "webdriverxx.h"

using namespace webdriverxx;

static CkGlobal glob;
bool unlockChilkat();

int main(int argc, char *argv[])
{
    if(!unlockChilkat()) return 1;

    const char* url = "http://localhost:9515";
    WebDriver gc = Start(Chrome(), url);


    QCoreApplication::setAttribute(Qt::AA_EnableHighDpiScaling);
    QGuiApplication app(argc, argv);

//    QQmlApplicationEngine engine;

//    engine.rootContext()->setContextProperty("AppMain", AppMain::instance());
//    engine.rootContext()->setContextProperty("AppModel", AppModel::instance());

//    LOGD << "[" << AppModel::instance()->screen_width() << "," << AppModel::instance()->screen_height() << "]";

//    const QUrl url(QStringLiteral("qrc:/qml/main.qml"));
//    QObject::connect(&engine, &QQmlApplicationEngine::objectCreated,
//                     &app, [url](QObject *obj, const QUrl &objUrl) {
//        if (!obj && url == objUrl)
//            QCoreApplication::exit(-1);
//    }, Qt::QueuedConnection);
//    engine.load(url);

    return app.exec();
}

bool unlockChilkat()
{
    qDebug() << "unlockChilkat";
    bool success_global = glob.UnlockBundle("AUTFRM.CB4082023_Pz2Ry7az86p4");
    if (!success_global)
    {
        qDebug() << "Error: " << glob.lastErrorText();
        return false;
    }

    int status = glob.get_UnlockStatus();
    if (status == 2)
    {
        qDebug() << "Unlocked using purchased unlock code.";
    }
    else
    {
        qDebug() << "Unlocked in trial mode.";
    }
    return true;
}
