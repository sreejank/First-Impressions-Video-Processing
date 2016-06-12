# First-Impressions-Video-Processing

This code was written to analyze short "talking head" videos for a research project. Specifically, it uses the OpenCV library
to process the video and calculate the entropy (Shannon's formula) of a person's body shift,
eye gaze, body tilt/posture, and language.

The testing videos used and survey data are currently not publicly available. 

Project abstract:

When we meet another person, we have the ability to make a quick first impression evaluation of 
their character traits. These evaluations are driven by our observations of others’ visual and
linguistic social cues. Although this evaluation is typically very subjective, an objective analysis
of these behavioral cues may be able to predict these evaluations. Moreover, it may be possible
to train a computer to predict these evaluations. To test this hypothesis, a mathematical analysis
was done on the entropy of the visual and linguistic cues of people featured in talking
head videos. These videos were shown to fifty human participants in a survey that asks about
participants’ first impressions for certain behavior traits such as gregariousness, intelligence, and dominance.
Multiple significant correlations between visual/linguistic entropies and survey responses were found, and a machine
learning classifier that was trained to use entropies to predict survey response had ~70% accuracy, showing that showing that
first impressions might be predictable using objective analysis of visual and linguistic cues.