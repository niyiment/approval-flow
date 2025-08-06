# Approval-Flow
A comprehensive file tracking and approval system built with Spring Boot and Spring State Machine. This system automates multi-level approvals for various organizational processes including leave requests, vendor payments, expense claims, and document approvals.

## Features
* Multi-level Approval Workflow: Configurable L1, L2, and L# approval levels
* State Machine: Robust state management using Spring State Machine
* Smart Routing: Automatic routing based on request type, amount, and business rules
* File tracking: Complete audit trail of all actions and state transitions
* Notifications:
  * Automated Reminders: Normal, warning, and escalation notification
  * Email alerts for all stakeholders
  * Threshold-based alerts
  
## Request Types Supported
* Leave Requests
* Vendor Payments
* Petty Cash Requests
* Document Approvals
* Purchase Orders
* Expense Claims
* Contract Approvals
  
## Technology Stack
* Backend: Spring Boot
* State Machine: Spring State Machine
* Database: H2. You can use any other RDMS like PostgreSQL/MySQL
* Email: Spring Mail
* Build Tool: Maven
* Java version: 21

## Installation
* Clone the project
  ```
  git clone https://github.com/niyiment/approval-flow/tree/main
  cd approval-flow
  ```
* Build project
  ```
    mvn clean install
  ```
* Run the application
  ```
    mvn spring-boot:run
  ```
  Open you browser and type in the Swagger url below:
    
    ```
    http://localhost:8080/swagger-ui/index.html
    ```

## Sample Approval Request
```
POST /api/approvals
Content-Type: application/json

{
"type": "EXPENSE_CLAIM",
"title": "Business Travel Expenses",
"description": "Travel expenses for client meeting",
"amount": 1200.00,
"submittedBy": "john.doe",
"submitterEmail": "john.doe@company.com",
"department": "SALES",
"priority": "NORMAL"
}
```

![img.png](img.png)