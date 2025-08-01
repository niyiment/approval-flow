package com.niyiment.approvalflow.config;

import com.niyiment.approvalflow.enums.ApprovalEvent;
import com.niyiment.approvalflow.enums.ApprovalState;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;

import java.util.HashMap;
import java.util.Map;

public class InMemoryStateMachinePersist implements StateMachinePersist<ApprovalState, ApprovalEvent, String> {
    
    private final Map<String, StateMachineContext<ApprovalState, ApprovalEvent>> storage = new HashMap<>();
    
    @Override
    public void write(StateMachineContext<ApprovalState, ApprovalEvent> context, String contextObj) throws Exception {
        storage.put(contextObj, context);
    }
    
    @Override
    public StateMachineContext<ApprovalState, ApprovalEvent> read(String contextObj) throws Exception {
        return storage.get(contextObj);
    }
}