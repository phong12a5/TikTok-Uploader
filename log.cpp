#include "log.h"
//#include "sqliteworker.h"
#include <QNetworkReply>

LogHelper* LogHelper::m_InstancePtr = nullptr;

LogHelper::LogHelper()
{
    if(nullptr == m_networkAccessManager) {
        m_networkAccessManager = new QNetworkAccessManager(this);
        connect(this, &LogHelper::writeLog, this, &LogHelper::writeLogHandle);
    }
}

LogHelper::~LogHelper()
{
    if(nullptr != m_networkAccessManager) {
        m_networkAccessManager->deleteLater();
    }
}

LogHelper *LogHelper::getInstance()
{
    if(nullptr == m_InstancePtr) {
        m_InstancePtr = new LogHelper();
    }
    return m_InstancePtr;
}

void LogHelper::deleteInstance()
{
    if(nullptr != m_InstancePtr) {
        delete m_InstancePtr;
        m_InstancePtr = nullptr;
    }
}

void LogHelper::initialize()
{
    if(m_initialize) {
        return;
    }
    // install qt message
#ifndef __DEBUG_MODE__
    m_requestForm.setUrl(QUrl("https://api8.fity.one/v1/logs/write"));
    m_requestForm.setRawHeader("Token", "df13e3b2-2801-11ec-a9da-985aeb8ef874");
    m_requestForm.setHeader(QNetworkRequest::ContentTypeHeader, "application/json");
    m_dataForm["AppName"] = QCoreApplication::applicationName();
    m_dataForm["device"] = QSysInfo::machineHostName();
    m_dataForm["app_version"] = APP_VER;

    qInstallMessageHandler(qtMessageHandler);
#endif
}

void LogHelper::writeLogHandle(QString msg)
{
//    m_dataForm["token"] = SQLiteWorker::getInstance()->getToken();
//    m_dataForm["log"] = msg;
//    QNetworkReply* reply = m_networkAccessManager->post(m_requestForm, QJsonDocument(m_dataForm).toJson(QJsonDocument::Compact));
//    connect(reply, &QNetworkReply::finished, this, [=]{
//        reply->deleteLater();
//    });
}

void LogHelper::qtMessageHandler(QtMsgType type, const QMessageLogContext &context, const QString &msg)
{
#if 0
    QByteArray localMsg = msg.toLocal8Bit();
    FILE * file;

#if 1   // write to file
    file = fopen ("log.txt", "a");
#else   // print in console
    fp = stderr;
#endif

    switch (type) {
    case QtDebugMsg:
        fprintf(file, "Debug: %s\n", localMsg.constData());
        break;
    case QtInfoMsg:
        fprintf(file, "Info: %s\n", localMsg.constData());
        break;
    case QtWarningMsg:
        fprintf(file, "Warning: %s\n", localMsg.constData());
        break;
    case QtCriticalMsg:
        fprintf(file, "Critical: %s\n", localMsg.constData());
        break;
    case QtFatalMsg:
        fprintf(file, "Fatal: %s\n", localMsg.constData());
        abort();
        break;
    }
    fclose(file);
#endif

    m_InstancePtr->writeLog(msg);
}
