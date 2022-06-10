#include "servicedata.h"
#include <QStringList>
#include <QDebug>
#include <QFileInfo>
#include <QDir>
#include <QTimer>
#include <QFile>
#include <QDebug>
#include <QFileInfoList>
#include <QFileInfo>
#include <QList>
#include "log.h"
#include <QSettings>
#include "afaction.h"
#include <QRandomGenerator>
#include <api/dbpapi.h>

ServiceData::ServiceData(AppEnum::SERVICE_TYPE type, int profileId, QObject *parent) :
    QObject(parent),
    m_type(type),
    m_profileId(profileId),
    m_cloneInfo(nullptr),
    m_proxy(nullptr),
    m_actions(nullptr)
{
    QSettings settings;
    switch (m_type) {
    case AppEnum::TYPE_CHROME_SERVICE:
        m_type_str = "chrome";
        break;
    case AppEnum::TYPE_FIREFOX_SERVICE:
        m_type_str = "firefox";
        break;
    default:
        m_type_str = "default";
        break;
    }

    m_profileFolderPath = QDir::currentPath() + "/" + m_type_str + "/";
    m_profilePath = m_profileFolderPath + "profiles/" + QString::number(profileId);

    loadCloneInfo();
}

ServiceData::~ServiceData()
{
    if(m_cloneInfo) {
        delete m_cloneInfo;
        m_cloneInfo = nullptr;
    }

    if(m_proxy) {
        delete m_proxy;
        m_proxy = nullptr;
    }

    if(m_actions) {
        m_actions->clear();
        delete m_actions;
        m_actions = nullptr;
    }
}

CloneInfo *ServiceData::cloneInfo()
{
    return m_cloneInfo;
}

void ServiceData::setCloneInfo(CloneInfo *cloneInfo)
{
    if(m_cloneInfo) {
        delete m_cloneInfo;
        m_cloneInfo = nullptr;
    }

    if(m_cloneInfo != cloneInfo) {
        m_cloneInfo = cloneInfo;

        // random userAgent
        if(m_cloneInfo) {
            connect(m_cloneInfo, &CloneInfo::cloneInfoChanged, this, &ServiceData::onCloneInfoChanged );
        }
    }
}

AFAction* ServiceData::getRandomAction()
{
    if(m_actions || !m_actions->empty()) {
       return m_actions->takeAt(QRandomGenerator::global()->bounded(m_actions->size()));
    } else {
        return nullptr;
    }
}

QList<AFAction*> *ServiceData::getActionList()
{
    return m_actions;
}

void ServiceData::setActionsList(QJsonArray array)
{
    LOGD;
    if(m_actions == nullptr) {
        m_actions = new QList<AFAction*>();
    } else {
        m_actions->clear();
    }

    for(int i = 0; i < array.size(); i++) {
        AFAction* action = new AFAction(array.at(i).toObject(), this);
        if(action) {
            switch (action->action_type()) {
                case AFAction::E_FACEBOOK_ACTION_FEED:
                case AFAction::E_FACEBOOK_ACTION_FEEDLIKE:
                m_actions->append(action);
                break;
            case AFAction::E_FACEBOOK_ACTION_PAGESUB:
                if(!action->fb_id().isEmpty()) {
                    m_actions->append(action);
                }else {
                    delete action;
                }
            default:
                delete action;
                break;
            }
        } else {
            delete action;
        }
    }
}

void ServiceData::loadCloneInfo()
{
    QSettings settings;
    QString cloneInfoPath = QString(CLONE_INFO_FILED).arg(m_type_str).arg(m_profileId);
    QJsonObject cloneInfo = settings.value(cloneInfoPath).toJsonObject();
    if(cloneInfo.isEmpty()) {
        setCloneInfo(nullptr);
        LOGD << "NULL";
    } else {
        setCloneInfo(new CloneInfo(cloneInfo));
        LOGD << QString("%1|%2").arg(m_cloneInfo->username(), m_cloneInfo->password());;
    }
}

void ServiceData::onCloneInfoChanged()
{
    QSettings settings;
    if(m_cloneInfo && m_cloneInfo->status() == CLONE_ALIVE_STATUS_STORE) {
        LOGD << "Save clone info: " << m_cloneInfo->username();
        settings.setValue(cloneInfokey(), m_cloneInfo->toJson());
        DBPApi::instance()->updateClone(m_cloneInfo->toString());
    } else {
        settings.setValue(cloneInfokey(), QJsonObject());
        LOGD << "Save clone info: NULL";
    }
}
