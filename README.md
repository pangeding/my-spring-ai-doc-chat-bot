# spring-ai-chat-doc
an ai chat doc bot base on Spring AI

## 0. preparation and file structure

### 0.1 Api key 
- use idea and open `run/debug configuration`
- fill in your own ai api `DASHSCOPE_API_KEY`
- using model tongyi(search on the Internet to get AliCloud free service)
### 0.2 open the frontend
- using nginx 
- just open it
### 0.3 run
- run the application in idea
- http://localhost:8080
## 1. basic
### 1.1 chat with the bot!
- http://localhost:8080/ai/generate(?message=abc) 正常
- http://localhost:8080/ai/generateStream(?message=abc) 流式

支持功能:
1. 上传PDF,TXT,MD 文件，基于文档实现问答程序

