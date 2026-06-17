package com.example.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.example.store.RedisChatMemoryStore;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/book/ai/")
@Api(tags = "AI模块")
public class ChatController {

    @Autowired
    QwenChatModel openAiChatModel;

    @GetMapping("chat")
    @ApiOperation(value = "测试使用")
    public String demo(@RequestParam(defaultValue = "你是谁") String message) {
        String chat = openAiChatModel.chat(message);
        return chat;
    }
    @Autowired
    private QwenStreamingChatModel streamingChatModel;
    /**
     * text/event-stream;charset=UTF-8 浏览器使用
     * text/plain;charset=UTF-8 swagger测试使用
     * @param message
     * @return
     */
    @GetMapping(value = "stream", produces = {"text/event-stream;charset=UTF-8", "text/plain;charset=UTF-8"})
    public Flux<String> stream(@RequestParam(defaultValue = "你是谁") String message) {
        // 1.必须加！主线程校验登录，最安全
        StpUtil.checkLogin();
        // 2. 再返回流式 Flux
        return Flux.create(fluxSink -> {
            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(UserMessage.from(message))
                    .build();

            streamingChatModel.chat(chatRequest, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    fluxSink.next(partialResponse);
                }

                @Override
                public void onCompleteResponse(ChatResponse chatResponse) {
                    fluxSink.complete();
                }

                @Override
                public void onError(Throwable throwable) {
                    fluxSink.error(throwable);
                }
            });
        });
    }
    @Autowired
    private RedisChatMemoryStore redisChatMemoryStore;
    /**
     * 最大消息条数（必须是偶数）
     * 一轮对话 = 2 条消息（用户消息 + AI 响应）
     * 40 条 = 20 轮对话
     */
    private static final int MAX_MESSAGES = 40;

    /**
     * 带聊天记忆的对话接口
     *
     * @param message 用户消息
     * @return AI 响应
     */
    @GetMapping("chat-memory")
    @ApiOperation(value = "带聊天记忆的对话",
            notes = "使用 Redis 存储聊天历史，支持多轮对话。自动保留最近 20 轮对话")
    public String chatWithMemory(
            @RequestParam(defaultValue = "你是谁，你能做什么？") String message) {

        // 1. 获取当前登录用户 ID 作为会话标识
        String loginId = StpUtil.getLoginId().toString();

        // 2. 获取历史消息（创建可变副本）
        List<ChatMessage> messages = new ArrayList<>(
                redisChatMemoryStore.getMessages(loginId)
        );

        // 3. 添加当前用户消息
        messages.add(UserMessage.from(message));

        // 4. 构建请求并调用 AI 模型
        ChatRequest request = ChatRequest.builder()
                .messages(messages)
                .build();
        ChatResponse response = openAiChatModel.chat(request);
        String responseText = response.aiMessage().text();

        // 5. 添加 AI 响应到历史消息
        messages.add(AiMessage.from(responseText));

        // 6. 限制消息数量（只保留最近的 MAX_MESSAGES 条）
        if (messages.size() > MAX_MESSAGES) {
            messages = messages.subList(
                    messages.size() - MAX_MESSAGES,
                    messages.size()
            );
        }

        // 7. 保存更新后的历史到 Redis
        redisChatMemoryStore.updateMessages(loginId, messages);

        return responseText;
    }
    @GetMapping("chat-enhanced")
    @ApiOperation("增强提示词对话")
    public String chatEnhanced(
            @RequestParam String message){

        StpUtil.checkLogin();
        String loginId = StpUtil.getLoginId().toString();

        // 1. 获取历史消息
        List<ChatMessage> messages = new ArrayList<>(
                redisChatMemoryStore.getMessages(loginId)
        );

        messages.add(0, SystemMessage.from(
                "你是数字图书管理系统的图书管理员，擅长根据用户需求推荐合适的书籍。" +
                        "每次推荐不超过3本，说明推荐理由。"
        ));

        // 3. 添加用户消息
        messages.add(UserMessage.from(message));

        // 4. 调用 AI
        ChatRequest request = ChatRequest.builder()
                .messages(messages)
                .build();
        ChatResponse response = qwenChatModel.chat(request);
        String result = response.aiMessage().text();

        // 5. 保存历史
        messages.add(AiMessage.from(result));
        if (messages.size() > MAX_MESSAGES) {
            messages = messages.subList(messages.size() - MAX_MESSAGES, messages.size());
        }
        redisChatMemoryStore.updateMessages(loginId, messages);

        return result;
    }
}
