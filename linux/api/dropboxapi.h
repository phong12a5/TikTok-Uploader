#ifndef DROPBOXAPI_H
#define DROPBOXAPI_H

#include <QObject>

class DropboxAPI : public QObject
{
    Q_OBJECT
    explicit DropboxAPI(QObject *parent = nullptr);

public:
    static DropboxAPI* instance();

    bool downloadFile(const char * path, const char * save_path);
signals:


private:
    static DropboxAPI* sInstance;

};

#endif // DROPBOXAPI_H
