#include <stdio.h>

int main(){
    char *http = "HTTP/1.1 200 OK\r\nContent-type: text/html\r\nContent-Length:38\r\n\r\n<html><body><h1>foi</h1></body></html>";
    printf("%s", http);
}