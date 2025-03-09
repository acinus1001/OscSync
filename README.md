### 사이드 프로젝트 모아둔 레포
- 쓰는 로직이 다 비슷해서 모노레포로 모아둔 다음 로직 공통화 후 재활용 위함
- 레포 이름 바꿔야됨

### 사용 스택

#### BE
- language : kotlin
- framework : spring boot
- db : pgsql + kotlin exposed 
- network : retrofit2

#### FE
- language : kotlin
- framework : jetbrain compose + decompose + mvi kotlin (kotlin/wasmJs)
- network : ktor client

### TODO (2025-03-09)
- 프론트 백 type safe request 구현
- Store 공통화 가능하면?
- jwt RefreshToken 구현
- spring batch 세팅
