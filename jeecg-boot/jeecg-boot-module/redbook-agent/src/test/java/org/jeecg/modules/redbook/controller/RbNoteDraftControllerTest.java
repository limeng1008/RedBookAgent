package org.jeecg.modules.redbook.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeecg.modules.redbook.entity.RbNoteDraft;
import org.jeecg.modules.redbook.model.req.RedbookAuditRequest;
import org.jeecg.modules.redbook.service.IRbNoteDraftService;
import org.jeecg.modules.redbook.service.IRedbookWorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RbNoteDraftController.class)
class RbNoteDraftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IRbNoteDraftService rbNoteDraftService;

    @MockBean
    private IRedbookWorkflowService redbookWorkflowService;

    @Test
    void approveShouldCallWorkflowServiceAndReturnOk() throws Exception {
        RbNoteDraft draft = new RbNoteDraft();
        draft.setId("draft-1");
        draft.setAuditStatus("approved");
        given(rbNoteDraftService.approveDraft("draft-1", "内容合规，可发布")).willReturn(draft);

        RedbookAuditRequest request = new RedbookAuditRequest();
        request.setId("draft-1");
        request.setAuditOpinion("内容合规，可发布");

        mockMvc.perform(post("/redbook/noteDraft/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("草稿已审核通过"))
            .andExpect(jsonPath("$.result.id").value("draft-1"))
            .andExpect(jsonPath("$.result.auditStatus").value("approved"));

        then(rbNoteDraftService).should().approveDraft("draft-1", "内容合规，可发布");
        then(rbNoteDraftService).should(never()).rejectDraft(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void rejectShouldCallWorkflowServiceAndReturnOk() throws Exception {
        RbNoteDraft draft = new RbNoteDraft();
        draft.setId("draft-2");
        draft.setAuditStatus("rejected");
        given(rbNoteDraftService.rejectDraft("draft-2", "标题口语化不足，需要重写"))
            .willReturn(draft);

        RedbookAuditRequest request = new RedbookAuditRequest();
        request.setId("draft-2");
        request.setAuditOpinion("标题口语化不足，需要重写");

        mockMvc.perform(post("/redbook/noteDraft/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("草稿已退回修改"))
            .andExpect(jsonPath("$.result.id").value("draft-2"))
            .andExpect(jsonPath("$.result.auditStatus").value("rejected"));

        then(rbNoteDraftService).should().rejectDraft("draft-2", "标题口语化不足，需要重写");
        then(rbNoteDraftService).should(never()).approveDraft(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }
}
