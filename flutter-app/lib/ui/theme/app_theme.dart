import 'package:flutter/material.dart';

// Mirrors Kotlin Color.kt
const _purple80 = Color(0xFFD0BCFF);
const _purpleGrey80 = Color(0xFFCCC2DC);
const _pink80 = Color(0xFFEFB8C8);

const _purple40 = Color(0xFF6650A4);
const _purpleGrey40 = Color(0xFF625B71);
const _pink40 = Color(0xFF7D5260);

final lightTheme = ThemeData(
  useMaterial3: true,
  colorScheme: ColorScheme.fromSeed(
    seedColor: _purple40,
    secondary: _purpleGrey40,
    tertiary: _pink40,
    brightness: Brightness.light,
  ),
);

final darkTheme = ThemeData(
  useMaterial3: true,
  colorScheme: ColorScheme.fromSeed(
    seedColor: _purple80,
    secondary: _purpleGrey80,
    tertiary: _pink80,
    brightness: Brightness.dark,
  ),
);
