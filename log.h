#ifndef LOG_H
#define LOG_H

#include <QDebug>
#include <QThread>
#include <QNetworkAccessManager>
#include <QJsonObject>
#include <qdatetime.h>

#define LOGD qDebug() << "[" << QThread::currentThreadId() << "][" << __FUNCTION__ << "][" << __LINE__ << "]/"

class LogHelper: public QObject
{
    Q_OBJECT

private:
    static LogHelper* m_InstancePtr;

    LogHelper();
    virtual ~LogHelper();

signals:
    void writeLog(QString msg);

public:
    static LogHelper* getInstance();
    static void deleteInstance();

    void initialize();

private slots:
    void writeLogHandle(QString msg);

protected:
    static void qtMessageHandler(QtMsgType type, const QMessageLogContext &context, const QString &msg);

private:
    bool m_initialize = false;

    // to send to server
    QNetworkAccessManager* m_networkAccessManager = nullptr;
    QNetworkRequest m_requestForm;
    QJsonObject m_dataForm;
};

#endif // LOG_H
