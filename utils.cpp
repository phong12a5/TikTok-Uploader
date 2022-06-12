#include "utils.h"
#include <QStringList>
#include <QThread>
#include <QProcess>
#include <log.h>
#include <QEventLoop>
#include <QTimer>

int compareVersion(QString version1, QString version2)
{
    Q_ASSERT(checkVersionFormat(version1));
    Q_ASSERT(checkVersionFormat(version2));
    QStringList ver1List = version1.split(".", Qt::SkipEmptyParts);
    QStringList ver2List = version2.split(".", Qt::SkipEmptyParts);
    QVector<int> ver1;
    QVector<int> ver2;

    ver1 << ver1List[0].toInt() << ver1List[1].toInt() << ver1List[2].toInt();
    ver2 << ver2List[0].toInt() << ver2List[1].toInt() << ver2List[2].toInt();
    if(ver1.at(0) < ver2.at(0)){
        return -1;
    } else if (ver1.at(0) > ver2.at(0)){
        return 1;
    } else {
        if(ver1.at(1) < ver2.at(1)){
            return -1;
        } else if (ver1.at(1) > ver2.at(1)){
            return 1;
        } else {
            if(ver1.at(2) < ver2.at(2)){
                return -1;
            } else if (ver1.at(2) > ver2.at(2)){
                return 1;
            } else return 0;
        }
    }
}

bool checkVersionFormat(QString verion)
{
    QString assertMessage;
    QStringList verList = verion.split(".", Qt::SkipEmptyParts);

    if(verList.size() != 3) {
        return false;
    } else {
        bool isOk0, isOk1, isOk2;
        verList[0].toInt(&isOk0);
        verList[1].toInt(&isOk1);
        verList[2].toInt(&isOk2);
        if(!(isOk0 && isOk1 && isOk2)){
            return false;
        } else {
            return true;
        }
    }
}

bool ping(QString host) {
#if defined(WIN32)
    QString parameter = "-n 1";
#else
  QString parameter = "-c 1";
#endif
    QString cmd = "ping -n 1 " + host;
    int exitCode = QProcess::execute(cmd);
    LOGD << cmd << " result: " << (exitCode == 0? "OK" : "KO");
    return exitCode == 0;
}

void handle_eptr(std::exception_ptr eptr) // passing by value is ok
{
    try {
        if (eptr) {
            std::rethrow_exception(eptr);
        } else {
            LOGD << "Unknown exception";
        }
    } catch(const std::exception& e) {
        LOGD << "Caught exception \"" << e.what();
    }
}

void delay(int milsec) {
    QEventLoop loop;
    QTimer::singleShot(milsec, &loop, SLOT(quit()));
    loop.exec();
}

int random(int min, int max)
{
    int randomValue = qrand() % max + min;
    return randomValue;
}

void delayRandom(int minMilSec, int maxMilSec)
{
    delay(random(minMilSec, maxMilSec));
}
