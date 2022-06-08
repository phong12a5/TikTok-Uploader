#ifndef APPMAIN_H
#define APPMAIN_H

#include <QObject>
#include "log.h"
#include <QProcess>
#include <QTimer>

class ServiceData;

class AppMain : public QObject
{
    Q_OBJECT
private:
    explicit AppMain(QObject *parent = nullptr);

public:
    static AppMain* instance();

    Q_INVOKABLE bool start();
    Q_INVOKABLE bool stop();

private:
    void setWindowProp(ServiceData* model, int index);

public slots:
    void onCheckPrecondition();
    void onServiceUpdated();
    void onConfigChanged();

private:
    static AppMain* m_instance;
    QProcess* m_chromeDriverProcess = nullptr;
    QTimer* m_preconditionChecker;
};

#endif // APPMAIN_H
