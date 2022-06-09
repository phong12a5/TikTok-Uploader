#include <QCoreApplication>
#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QQmlContext>
#include <appmain.h>
#include <appmodel.h>
#include <CkGlobal.h>
#include "log.h"

/*
#include "webdriverxx.h"

using namespace webdriverx*/;

static CkGlobal glob;
bool unlockChilkat();

int main(int argc, char *argv[])
{
    if(!unlockChilkat()) return 1;

    QProcess::execute("pkill -9 -f chromedriver");
    QProcess::execute("pkill -9 -f chrome");

    QCoreApplication::setAttribute(Qt::AA_EnableHighDpiScaling);
    QGuiApplication app(argc, argv);

    QQmlApplicationEngine engine;

    engine.rootContext()->setContextProperty("AppMain", AppMain::instance());
    engine.rootContext()->setContextProperty("AppModel", AppModel::instance());

    LOGD << "[" << AppModel::instance()->screen_width() << "," << AppModel::instance()->screen_height() << "]";

    const QUrl url(QStringLiteral("qrc:/qml/main.qml"));
    QObject::connect(&engine, &QQmlApplicationEngine::objectCreated,
                     &app, [url](QObject *obj, const QUrl &objUrl) {
        if (!obj && url == objUrl)
            QCoreApplication::exit(-1);
    }, Qt::QueuedConnection);
    engine.load(url);

    return app.exec();
}

bool unlockChilkat()
{
    LOGD << "unlockChilkat";
    bool success_global = glob.UnlockBundle("AUTFRM.CB4082023_Pz2Ry7az86p4");
    if (!success_global)
    {
        LOGD << "Error: " << glob.lastErrorText();
        return false;
    }

    int status = glob.get_UnlockStatus();
    if (status == 2)
    {
        LOGD << "Unlocked using purchased unlock code.";
    }
    else
    {
        LOGD << "Unlocked in trial mode.";
    }
    return true;
}
