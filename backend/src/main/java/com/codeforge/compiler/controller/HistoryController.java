package com.codeforge.compiler.controller;

import com.codeforge.compiler.dto.history.HistoryResponse;
import com.codeforge.compiler.service.HistoryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

  private final HistoryService historyService;

  public HistoryController(HistoryService historyService) {
    this.historyService = historyService;
  }

  @GetMapping
  public List<HistoryResponse> history() {
    return historyService.myHistory();
  }
}