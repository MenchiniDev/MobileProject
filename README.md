
# Narciso: Understanding User Preferences through Wearable Sensors

**Authors**: Alessio Franchini, Cristina Maria Rita Lombardo, Lorenzo Menchini, Alice Petrillo

## Overview

Narciso is a mobile application designed to capture user preferences by collecting physiological and behavioral data while the user views images. The app integrates data from wearable sensors, such as EEG, heart rate, PPG, and EDA, alongside facial expression analysis, to classify user preferences through machine learning.

## Features

- **Multi-sensor Integration**: Collects EEG data, facial expressions, heart rate, PPG, and EDA.
- **User Interaction**: Users vote on displayed images to label preference data.
- **Machine Learning Classification**: Uses captured data to predict preferences with a supervised classifier.

## Architecture

1. **Smartphone**: Central controller for data collection and image display.
2. **Smartwatch**: Collects heart rate, PPG, and EDA.
3. **Mindrove EEG Helmet**: Captures brain activity.
4. **Firebase Database**: Stores all collected data for model training.

## Setup Instructions

1. **Requirements**:
   - Compatible smartphone, smartwatch, and EEG helmet.
   - Firebase account for data storage.
2. **Installation**:
   - Clone the repository.
   - Install required libraries (list dependencies).
   - Configure Firebase database access.

## Usage

1. Connect the devices (smartphone, smartwatch, and helmet).
2. Launch the app, display images, and capture user reactions.
3. Access stored data for training and analysis.

## Data Analysis

Data collected is used to train a machine learning model to predict user preferences, with comparative accuracy based on different sensor inputs (e.g., EEG vs. non-EEG).

## Applications

Narciso can be applied to:
- **Personalized Content Delivery**: Tailors user experiences in social media and streaming.
- **Advertising Analysis**: Captures reactions to promotional content.
- **Museum and Art Galleries**: Understand visitor engagement with exhibits.

## License

This project is licensed under the MIT License.
