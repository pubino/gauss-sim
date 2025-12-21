# Gaussian Elimination Simulator

Part of [Vanderbei's Java Simulations](https://github.com/pubino/vanderbei-apps) collection.

An interactive Java application for learning and practicing Gaussian elimination. Originally by Professor Robert Vanderbei, Princeton University (1997).

## Overview

This educational tool allows users to:
- Perform step-by-step Gaussian elimination on systems of linear equations
- Generate random systems for practice
- Switch between decimal and fraction display modes
- Swap rows to handle pivot selection

## Features

- **Configurable Matrix Size**: Set rows and columns, then resize dynamically
- **Interactive Row Operations**: Click variable buttons to perform elimination steps
- **Row Swapping**: Use checkboxes to swap any two rows
- **Display Modes**: Toggle between decimal and fraction representations
- **Random Problem Generator**: Create random systems for practice

## Requirements

- Java 14+ (for running the JAR)
- Java 14+ with `jpackage` (for building macOS app bundle)

## Building

```bash
# Build the JAR file
make jar

# Build macOS .app bundle (unsigned)
make app
```

## Running

```bash
# Run the JAR
make run

# Or run directly
java -jar dist/GaussSim.jar
```

## Usage

### Setting Up a System

1. Enter the number of rows and columns
2. Click **Resize** to create the matrix
3. Enter coefficients in the matrix fields
4. Enter right-hand side values in the last column
5. Or click **Generate Random Problem** for a random system

### Performing Elimination

The system is displayed as: a₁₁x₁ + a₁₂x₂ + ... = b₁

**Diagonal Operations** (pivot rows):
- Click a variable button on the diagonal (highlighted in pink)
- The entire row is divided by that coefficient
- Result: coefficient becomes 1

**Off-Diagonal Operations** (elimination):
- Click a variable button off the diagonal
- The corresponding row is subtracted (scaled appropriately)
- Result: coefficient becomes 0

### Row Swapping

1. Check the checkbox next to the first row
2. Check the checkbox next to the second row
3. Rows are automatically swapped

### Display Options

- **Decimal**: Show all values as decimal numbers
- **Fraction**: Show values as fractions when possible

## Project Structure

```
gauss-sim/
├── gauss/
│   └── GaussElim.java      # Main app, InPanel, myButton, myCheckbox
├── myutil/
│   ├── AppletFrame.java    # Applet-to-Frame wrapper (unused)
│   ├── GridPanel.java      # Grid layout panel
│   ├── NumberField.java    # Numeric input field with fraction support
│   └── Format.java         # Number formatting utilities
├── Makefile                # Build configuration
└── .github/workflows/      # CI/CD configuration
```

## Algorithm

Gaussian elimination transforms a system Ax = b into row echelon form:

1. **Forward Elimination**:
   - For each pivot row, divide by the pivot element
   - Eliminate entries below the pivot

2. **Back Substitution** (optional):
   - Eliminate entries above pivots
   - Result: reduced row echelon form (identity matrix for square systems)

## Visual Indicators

- **Pink cells**: Diagonal elements (pivot positions)
- **Variable buttons** (x1, x2, ...): Click to perform row operations
- **Checkboxes**: Select rows for swapping
