#ifndef LOG_H
#define LOG_H

#include <QDebug>
#include <QThread>

#define LOGD qDebug() << "[" << QThread::currentThreadId() << "][" << __FUNCTION__ << "][" << __LINE__ << "]/"
#endif // LOG_H
