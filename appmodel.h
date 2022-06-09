#ifndef APPMODEL_H
#define APPMODEL_H

#include <QObject>
#include <QSettings>

class AppModel : public QObject
{
    Q_OBJECT
    Q_PROPERTY(bool appStarted READ appStarted WRITE setAppStarted NOTIFY appStartedChanged)
    Q_PROPERTY(int maxThread READ maxThread WRITE setMaxThread NOTIFY maxThreadChanged)
    Q_PROPERTY(QString deviceName READ deviceName CONSTANT)
private:
    explicit AppModel();

public:
    static AppModel* instance();

public:
    bool appStarted();
    void setAppStarted(bool state);

    int maxThread();
    void setMaxThread(int max);

    QString deviceName();
    QString appVersion();

    int latestProfileId();
    void setLatestProfileId(int id);

    int runningBrowser();
    void setRunningBrowser(int count);

    int screen_width();
    int screen_height();
signals:
    void appStartedChanged();
    void maxThreadChanged();
    void deviceStatusChanged();

private:
    static AppModel* sInstance;

    bool m_appStarted;
    int m_maxThread;
};

#endif // APPMODEL_H
