package dev.kuro9.module.front.application.homepage.page.admin.resource_manage

import androidx.compose.runtime.*
import dev.kuro9.module.front.application.homepage.components.CopyableText
import dev.kuro9.module.front.application.homepage.network.AuthResourceManageApiService
import dev.kuro9.module.front.application.homepage.utils.requireAnyAuthority
import dev.kuro9.multiplatform.common.network.ServerInfo
import dev.kuro9.multiplatform.common.types.app.homepage.common.ImageResourceListResponse
import dev.kuro9.multiplatform.common.types.app.homepage.common.StringResourceListResponse
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.name
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.attributes.rows
import org.jetbrains.compose.web.css.minHeight
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.*
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.koin.compose.koinInject
import org.w3c.files.File
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.uuid.ExperimentalUuidApi

enum class ManageMode { TEXT, IMAGE }

@OptIn(ExperimentalUuidApi::class)
@Composable
fun AdminResourceManage() = requireAnyAuthority("ROLE_ROOT") { isLoading ->
    if (isLoading) return@requireAnyAuthority

    // API 서비스 및 코루틴 스코프 설정
    val scope = rememberCoroutineScope()

    // 상태 관리
    var currentMode by remember { mutableStateOf(ManageMode.TEXT) }
    var stringList by remember { mutableStateOf<List<StringResourceListResponse.Element>>(emptyList()) }
    var imageList by remember { mutableStateOf<List<ImageResourceListResponse.Element>>(emptyList()) }

    val service: AuthResourceManageApiService = koinInject()
    val serverInfo: ServerInfo = koinInject()

    // 데이터 로딩 로직
    fun loadData() {
        scope.launch {
            if (currentMode == ManageMode.TEXT) {
                stringList = service.getStringResourceList().resources
            } else {
                imageList = service.getImageResourceList().resources
            }
        }
    }

    // 모드 변경 시 데이터 로드
    LaunchedEffect(currentMode) {
        loadData()
    }

    Div {
        // --- 상단 모드 선택 ---
        H1 { Text("Admin Resource Management") }

        Hr()
        Div {
            Label {
                RadioInput(
                    checked = currentMode == ManageMode.TEXT,
                    attrs = {
                        name("TEXT")
                        onClick { currentMode = ManageMode.TEXT }
                    }
                )
                Text("TEXT RESOURCES")
            }

            Label {
                RadioInput(
                    checked = currentMode == ManageMode.IMAGE,
                    attrs = {
                        name("IMAGE")
                        onClick { currentMode = ManageMode.IMAGE }
                    }
                )
                Text("IMAGE RESOURCES")
            }
        }
        Hr()

        // --- 텍스트 관리 섹션 ---
        if (currentMode == ManageMode.TEXT) {
            H2 { Text("Text Resources") }
            Table(attrs = { }) {
                Thead {
                    Tr {
                        Th { Text("ID(Click to Copy)") }
                        Th { Text("Description") }
                        Th { Text("Authority") }
                        Th { Text("Value") }
                        Th { Text("Action") }
                    }
                }
                Tbody {
                    // 기존 리스트 출력 및 수정
                    stringList.forEach { element ->
                        StringRow(element, onUpdate = { desc, allowed, str ->
                            scope.launch {
                                service.modifyStringResource(
                                    element.externalId.toString(),
                                    string = str,
                                    allowedAuthorities = allowed,
                                    description = desc,
                                )
                                loadData() // 갱신
                                window.alert("수정 완료")
                            }
                        })
                    }

                    // 새 리소스 등록 행
                    NewStringRow(onAdd = { desc, allowed, str ->
                        scope.launch {
                            service.postNewStringResource(
                                string = str,
                                allowedAuthorities = allowed,
                                description = desc
                            )
                            loadData() // 갱신
                        }
                    })
                }
            }
        }

        // --- 이미지 관리 섹션 ---
        else {
            H2 { Text("Image Resources") }

            Table(attrs = { }) {
                Thead {
                    Tr {
                        Th { Text("ID") }
                        Th { Text("Description") }
                        Th { Text("Authority") }
                        Th { Text("Image") }
                        Th { Text("Action") }
                    }
                }
                Tbody {
                    // 기존 리스트 출력 및 수정
                    imageList.forEach { element ->
                        ImageRow(serverInfo, element, onUpdate = { desc, allowed ->
                            scope.launch {
                                service.modifyImageResource(
                                    element.externalId.toString(),
                                    allowedAuthorities = allowed,
                                    description = desc,
                                )
                                loadData() // 갱신
                                window.alert("수정 완료")
                            }
                        })
                    }

                    // 새 리소스 등록 행
                    NewImageRow(onAdd = { desc, allowed, file ->
                        scope.launch {
                            val reader = FileReader()
                            reader.readAsArrayBuffer(file)
                            reader.onload = {
                                val arrayBuffer = reader.result as ArrayBuffer
                                val byteArray = Int8Array(arrayBuffer).asDynamic() as ByteArray
                                scope.launch {
                                    service.postNewImageResource(
                                        image = byteArray,
                                        contentType = file.type,
                                        allowedAuthorities = allowed,
                                        description = desc
                                    )
                                    loadData() // 갱신
                                }
                            }
                        }
                    })
                }
            }
        }
    }
}

/**
 * 이미지 리소스 수정을 위한 개별 행 컴포저블
 */
@OptIn(ExperimentalUuidApi::class)
@Composable
fun ImageRow(
    serverInfo: ServerInfo,
    element: ImageResourceListResponse.Element,
    onUpdate: (description: String?, allowed: List<String>?) -> Unit
) {

    var desc: String? by remember { mutableStateOf(element.description) }
    var allowed: List<String> by remember { mutableStateOf(element.allowed) }

    Tr {
        Td { CopyableText(element.externalId.toString()) }
        Td {
            Input(type = InputType.Text) {
                value(desc ?: "")
                onInput { desc = it.value }
            }
        }
        Td {
            Input(type = InputType.Text) {
                value(allowed.joinToString(", "))
                onInput { allowed = it.value.split(",").map(String::trim) }
            }
        }
        Td {
            A(href = "$serverInfo/resources/images/${element.externalId}") {
                Button { Text("VIEW IMAGE") }
            }
        }
        Td {
            Button(attrs = { onClick { onUpdate(desc, allowed) } }) {
                Text("MODIFY")
            }
        }
    }
}

/**
 * 새 이미지 리소스 등록을 위한 하단 빈 행
 */
@Composable
fun NewImageRow(onAdd: (description: String?, allowed: List<String>, file: File) -> Unit) {
    var newDesc: String? by remember { mutableStateOf(null) }
    var newAllowed: List<String> by remember { mutableStateOf(emptyList<String>()) }
    var selectedFile: File? by remember { mutableStateOf(null) }

    Tr {
        Td { Text("(New)") }
        Td {
            Input(type = InputType.Text) {
                value(newDesc ?: "")
                onInput { newDesc = it.value }
                placeholder("Description")
            }
        }
        Td {
            Input(type = InputType.Text) {
                value(newAllowed.joinToString(", "))
                onInput { newAllowed = it.value.split(",").map(String::trim) }
                placeholder("Allowed Authorities")
            }
        }
        Td {
            Input(type = InputType.File) {
                onChange { event ->
                    val files = event.target.files
                    if (files != null && files.length > 0) {
                        selectedFile = files[0]
                    }
                }
            }
        }
        Td {
            Button(attrs = {
                onClick {
                    val file = selectedFile
                    if (file != null) {
                        onAdd(newDesc, newAllowed, file)

                        // 입력창 초기화
                        newDesc = null
                        newAllowed = emptyList()
                        selectedFile = null
                    }
                }
            }) {
                Text("POST")
            }
        }
    }
}

/**
 * 텍스트 리소스 수정을 위한 개별 행 컴포저블
 */
@OptIn(ExperimentalUuidApi::class)
@Composable
fun StringRow(
    element: StringResourceListResponse.Element,
    onUpdate: (description: String?, allowed: List<String>?, value: String?) -> Unit
) {
    var desc: String? by remember { mutableStateOf(element.description) }
    var allowed: List<String> by remember { mutableStateOf(element.allowed) }
    var strValue: String by remember { mutableStateOf(element.string) }

    Tr {
        Td { CopyableText(element.externalId.toString()) }
        Td {
            Input(type = InputType.Text) {
                value(desc ?: "")
                onInput { desc = it.value }
            }
        }
        Td {
            Input(type = InputType.Text) {
                value(allowed.joinToString(", "))
                onInput { allowed = it.value.split(",").map(String::trim) }
            }
        }
        Td {
            TextArea {
                value(strValue)
                onInput { strValue = it.value }
                rows(1)
                style {
                    width(360.px)
                    minHeight(15.px)
                    property("resize", "vertical")
                }
            }
        }
        Td {
            Button(attrs = { onClick { onUpdate(desc, allowed, strValue) } }) {
                Text("MODIFY")
            }
        }
    }
}

/**
 * 새 텍스트 리소스 등록을 위한 하단 빈 행
 */
@Composable
fun NewStringRow(onAdd: (description: String?, allowed: List<String>, value: String) -> Unit) {
    var newDesc: String? by remember { mutableStateOf(null) }
    var newAllowed: List<String> by remember { mutableStateOf(emptyList<String>()) }
    var newStr: String by remember { mutableStateOf("") }

    Tr {
        Td { Text("(New)") }
        Td {
            Input(type = InputType.Text) {
                value(newDesc ?: "")
                onInput { newDesc = it.value }
                placeholder("Description")
            }
        }
        Td {
            Input(type = InputType.Text) {
                value(newAllowed.joinToString(", "))
                onInput { newAllowed = it.value.split(",").map(String::trim) }
                placeholder("Allowed Authorities")
            }
        }
        Td {
            TextArea {
                value(newStr)
                onInput { newStr = it.value }
                placeholder("Value")
                rows(1)
                style {
                    width(360.px)
                    minHeight(15.px)
                    property("resize", "vertical")
                }
            }
        }
        Td {
            Button(attrs = {
                onClick {
                    onAdd(newDesc, newAllowed, newStr)

                    // 입력창 초기화
                    newDesc = null
                    newAllowed = emptyList()
                    newStr = ""
                }
            }) {
                Text("POST")
            }
        }
    }
}