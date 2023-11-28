package com.example.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun MainPage(
    stitcherViewModel: StitcherViewModel,
    cameraViewModel: CameraViewModel
) {
    MainContent(stitcherViewModel, cameraViewModel)
}

@Composable
private fun MainContent(
    stitcherViewModel: StitcherViewModel,
    cameraViewModel: CameraViewModel
) {
    val dummyNavigationState = rememberSaveable { mutableStateOf(true) }

    stitcherViewModel.chooseImages()

    Column(
        Modifier.selectableGroup().fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Button(
            onClick = {
                dummyNavigationState.value = !dummyNavigationState.value
            }
        ) {
            if (dummyNavigationState.value) {
                Text("Camera Page")
            } else {
                Text("Main Page")
            }
        }

        if (dummyNavigationState.value) {
            MainPageContent(stitcherViewModel)

        } else {
            CameraPage(stitcherViewModel, cameraViewModel)
        }
    }
}

@Composable
fun MainPageContent(stitcherViewModel: StitcherViewModel) {

    val radioOptions = listOf("Scans", "Panorama")

    val (selectedOption: String, onOptionSelected: (String) -> Unit) = rememberSaveable {
        mutableStateOf(
            radioOptions[0]
//            radioOptions[1]
        )
    }

//    val imageBitmap: MutableState<ImageBitmap?> = stitcherViewModel.imageBitmap

    Column(
        Modifier.selectableGroup().fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row {
            radioOptions.forEachIndexed { index, text ->
                SelectOptionsCheckout(
                    text = text,
                    isSelectedOption = selectedOption == text,
                    onSelectOption = onOptionSelected
                )
                if (index == 0) {
                    Spacer(Modifier.width(20.dp))
                }
            }
        }

        Row(Modifier.height(20.dp)) {
            Text("Use CLAHE")
            Spacer(Modifier.width(5.dp))
            Checkbox(
                checked = stitcherViewModel.claheState.value,
                onCheckedChange = { stitcherViewModel.changeClaheState(it) },
            )
        }

        Button(
            onClick = {
                stitcherViewModel.isScansChecked.value = selectedOption == "Scans"
                stitcherViewModel.isOpenGallery.value = true
                stitcherViewModel.setUpStitcher()
            }
        ) {
            Text("Choose Images")
        }

        stitcherViewModel.imageBitmap.value?.let {
            Image(bitmap = it, contentDescription = null)
        }
    }
}

@Composable
fun CheckboxResource(isSelected: Boolean): ImageVector {
    return if (isSelected) {
        Icons.Default.Check
    } else {
        Icons.Default.Cancel
    }
}

@Composable
fun SelectOptionsCheckout(
    text: String,
    isSelectedOption: Boolean,
    onSelectOption: (String) -> Unit
) {
    Text(text)
    Icon(
        imageVector = CheckboxResource(isSelected = isSelectedOption),
        contentDescription = "Checkbox",
        modifier = Modifier
            .clickable {
                onSelectOption(text)
            },
    )
}

/*@Composable
fun SelectOptionsCheckout(
    index: Int,
    text: String,
    isSelectedOption: Boolean,
    onSelectOption: (Int) -> Unit
) {

    Row() {
        Text(text)
        Icon(
            imageVector = CheckboxResource(isSelected = isSelectedOption),
            contentDescription = "Checkbox",
            modifier = Modifier
                .clickable {
                    onSelectOption(index)
                }
        )
    }
}

@Composable
private fun CheckBoxGroup() {
    val radioOptions = listOf("OptionA", "OptionB", "OptionC")

    val (selectedOption: Int, onOptionSelected: (Int) -> Unit) = remember {
        mutableStateOf(
            -1
        )
    }

    Column(Modifier.selectableGroup()) {
        radioOptions.forEachIndexed { index, text ->
            SelectOptionsCheckout(
                index = index,
                text = text,
                isSelectedOption = selectedOption == index,
                onSelectOption = {
                    if (it == selectedOption) {
                        onOptionSelected(-1)
                    } else {
                        onOptionSelected(it)
                    }
                }
            )
        }
    }
}*/
