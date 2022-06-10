#include "cloneinfo.h"
#include "log.h"

CloneInfo::CloneInfo(QJsonObject cloneInfo)
{
    m_cloneInfo = cloneInfo;
}

QString CloneInfo::username()
{
    return m_cloneInfo.value(CLONE_INFO_FIELD_USERNAME).toString();
}

void CloneInfo::setPassWord(QString passWord)
{
    if(this->password() != passWord) {
        m_cloneInfo[CLONE_INFO_FIELD_PASSWORD] = passWord;
        emit cloneInfoChanged();
    }
}

QString CloneInfo::password()
{
    return m_cloneInfo.value(CLONE_INFO_FIELD_PASSWORD).toString();
}

QString CloneInfo::email()
{
    return m_cloneInfo.value(CLONE_INFO_FIELD_EMAIL).toString();
}

void CloneInfo::setEmail(QString email)
{
    if(this->email() != email) {
        m_cloneInfo[CLONE_INFO_FIELD_EMAIL] = email;
        emit cloneInfoChanged();
    }
}

qint64 CloneInfo::lastUploadTime()
{
    QString timestamp = m_cloneInfo.value(CLONE_INFO_FIELD_LAST_UPLOAD_TIME).toString();
    return timestamp.toLongLong();
}

void CloneInfo::setLastUploadTime(qint64 time)
{
    if(this->lastUploadTime() != time) {
        m_cloneInfo[CLONE_INFO_FIELD_LAST_UPLOAD_TIME] = time;
        emit cloneInfoChanged();
    }
}

QString CloneInfo::clonedFrome()
{
    return m_cloneInfo.value(CLONE_INFO_FIELD_CLONED_FROM).toString();
}

QString CloneInfo::videoFolderPath()
{
    return m_cloneInfo.value(CLONE_INFO_FIELD_VIDEO_FOLDER_PATH).toString();
}

QString CloneInfo::status()
{
    return m_cloneInfo.value(CLONE_INFO_FIELD_STATUS).toString();
}

void CloneInfo::setStatus(QString status)
{
    if(this->status() != status) {
        m_cloneInfo[CLONE_INFO_FIELD_STATUS] = status;
        emit cloneInfoChanged();
    }
}

QJsonObject CloneInfo::toJson()
{
    return m_cloneInfo;
}

QString CloneInfo::toString()
{
    QJsonDocument doc(m_cloneInfo);
    QString strJson(doc.toJson(QJsonDocument::Compact));
    return strJson;
}
