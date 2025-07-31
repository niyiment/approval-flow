package com.niyiment.approvalflow;


import com.niyiment.approvalflow.enums.ApprovalEvent;
import com.niyiment.approvalflow.enums.ApprovalState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

import java.util.EnumSet;

@Configuration
@EnableStateMachine
@Slf4j
public class StateMachineConfig extends StateMachineConfigurerAdapter<ApprovalState, ApprovalEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<ApprovalState, ApprovalEvent> states) throws Exception {
        states.withStates()
                .initial(ApprovalState.DRAFT)
                .states(EnumSet.allOf(ApprovalState.class))
                .end(ApprovalState.PROCESSED)
                .end(ApprovalState.REJECTED)
                .end(ApprovalState.CANCELLED)
                .end(ApprovalState.EXPIRED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<ApprovalState, ApprovalEvent> transitions) throws Exception {
        transitions
                .withExternal()
                .source(ApprovalState.DRAFT)
                .target(ApprovalState.SUBMITTED)
                .event(ApprovalEvent.SUBMIT)
                .action(context -> {
                    log.info("Request submitted for approval");
                })

                .and()
                .withExternal()
                .source(ApprovalState.SUBMITTED)
                .target(ApprovalState.L1_APPROVED)
                .guard(context -> true)
                .and()

                .withExternal()
                .source(ApprovalState.L1_PENDING)
                .target(ApprovalState.L1_APPROVED)
                .event(ApprovalEvent.L1_APPROVE)
                .action(context -> {
                    log.info("Level 1 approval granted");
                })
                .and()

                .withExternal()
                .source(ApprovalState.L1_PENDING)
                .target(ApprovalState.REJECTED)
                .event(ApprovalEvent.L1_REJECT)
                .action(context -> {
                    log.info("Request rejected at Level 1");
                })
                .and()

                //level 1 to level 2 routing
                .withExternal()
                .source(ApprovalState.L1_APPROVED)
                .target(ApprovalState.L2_PENDING)
                .guard(stateContext -> true)
                .and()

                // level 2 Approval flow
                .withExternal()
                .source(ApprovalState.L2_PENDING)
                .target(ApprovalState.L2_APPROVED)
                .event(ApprovalEvent.L2_APPROVE)
                .action(context -> {
                    log.info("Level 2 approval granted");
                })
                .and()

                .withExternal()
                .source(ApprovalState.L2_PENDING)
                .target(ApprovalState.REJECTED)
                .event(ApprovalEvent.L2_REJECT)
                .action(context -> {
                    log.info("Request rejected at Level 2");
                })
                .and()

                // Level 2 to Level 3 or Final routing
                .withExternal()
                .source(ApprovalState.L2_APPROVED)
                .target(ApprovalState.L3_PENDING)
                .guard(context -> {
                    // Check if L3 approval required (high value, special types)
                    Object requiresL3 = context.getExtendedState().getVariables().get("requiresL3");
                    return requiresL3 != null && (Boolean) requiresL3;
                })
                .and()

                .withExternal()
                .source(ApprovalState.L2_APPROVED)
                .target(ApprovalState.FINAL_APPROVED)
                .event(ApprovalEvent.FINAL_APPROVE)
                .guard(context -> {
                    // Skip L3 if not required
                    Object requiresL3 = context.getExtendedState().getVariables().get("requiresL3");
                    return requiresL3 == null || !(Boolean) requiresL3;
                })
                .and()

                // Level 3 Approval Flow
                .withExternal()
                .source(ApprovalState.L3_PENDING)
                .target(ApprovalState.L3_APPROVED)
                .event(ApprovalEvent.L3_APPROVE)
                .action(context -> {
                    log.info("Level 3 approval granted");
                })
                .and()

                .withExternal()
                .source(ApprovalState.L3_PENDING)
                .target(ApprovalState.REJECTED)
                .event(ApprovalEvent.L3_REJECT)
                .action(context -> {
                    log.info("Request rejected at Level 3");
                })
                .and()

                .withExternal()
                .source(ApprovalState.L3_APPROVED)
                .target(ApprovalState.FINAL_APPROVED)
                .event(ApprovalEvent.FINAL_APPROVE)
                .and()

                // Final processing
                .withExternal()
                .source(ApprovalState.FINAL_APPROVED)
                .target(ApprovalState.PROCESSED)
                .event(ApprovalEvent.PROCESS)
                .action(context -> {
                    log.info("Request processing completed");
                })
                .and()

                // Cancellation from any state (except final states)
                .withExternal()
                .source(ApprovalState.DRAFT)
                .target(ApprovalState.CANCELLED)
                .event(ApprovalEvent.CANCEL)
                .and()
                .withExternal()
                .source(ApprovalState.SUBMITTED)
                .target(ApprovalState.CANCELLED)
                .event(ApprovalEvent.CANCEL)
                .and()
                .withExternal()
                .source(ApprovalState.L1_PENDING)
                .target(ApprovalState.CANCELLED)
                .event(ApprovalEvent.CANCEL)
                .and()
                .withExternal()
                .source(ApprovalState.L2_PENDING)
                .target(ApprovalState.CANCELLED)
                .event(ApprovalEvent.CANCEL)
                .and()
                .withExternal()
                .source(ApprovalState.L3_PENDING)
                .target(ApprovalState.CANCELLED)
                .event(ApprovalEvent.CANCEL)
                .and()

                // Expiration transitions
                .withExternal()
                .source(ApprovalState.L1_PENDING)
                .target(ApprovalState.EXPIRED)
                .event(ApprovalEvent.EXPIRE)
                .and()
                .withExternal()
                .source(ApprovalState.L2_PENDING)
                .target(ApprovalState.EXPIRED)
                .event(ApprovalEvent.EXPIRE)
                .and()
                .withExternal()
                .source(ApprovalState.L3_PENDING)
                .target(ApprovalState.EXPIRED)
                .event(ApprovalEvent.EXPIRE);

    }

    @Bean
    public StateMachineListener<ApprovalState, ApprovalEvent> stateMachineListener() {
        return new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<ApprovalState, ApprovalEvent> from,
                                     State<ApprovalState, ApprovalEvent> to) {
                log.info("State changed from {} to {}",
                        from != null ? from.getId() : "none", to.getId());
            }

            @Override
            public void transition(Transition<ApprovalState, ApprovalEvent> transition) {
                log.info("Transition: {} -> {} on event {}",
                        transition.getSource().getId(),
                        transition.getTarget().getId(),
                        transition.getTrigger().getEvent());
            }

        };
    }
}
