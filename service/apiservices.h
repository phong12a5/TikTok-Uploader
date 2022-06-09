#ifndef APISERVICES_H
#define APISERVICES_H

#include <QObject>
#include <QThread>
#include <QTimer>
#include <QJsonDocument>
#include <QJsonObject>

class APIServices : public QObject
{
    Q_OBJECT
public:
    static APIServices* instance();

    void startService();
    bool isFdriverReady();
    bool isAFAPIReady();
    bool isDeviceApproved();
    QString deviceStatus();

    QJsonObject config();
    void setConfig(QJsonObject config);

private:
    explicit APIServices(QObject *parent = nullptr);

public slots:
    void onStarted();

private:
    QThread* m_subThread;
    QTimer* m_checker;


    bool m_isFdriverReady;
    bool m_isAFAPIReady;
    QJsonObject m_config;

signals:
    void configChanged();
};

#endif // APISERVICES_H
