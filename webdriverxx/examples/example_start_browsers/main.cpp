#include "webdriverxx.h"

using namespace webdriverxx;

int main()
{
  const char* url = "http://localhost:9515";

  // WebDriver ff = Start(Firefox());

  // WebDriver op = Start(Opera());

  // WebDriver ph = Start(PhantomJS());

  WebDriver gc = Start(Chrome(), url);

  // WebDriver ie = Start(InternetExplorer());

  getchar();

  return 0;
}
