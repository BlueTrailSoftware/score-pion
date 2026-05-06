//messages for all inputs of signup flow
export const signUpFormErrorMessages = {
  firstNameError: {
    required: 'First name is required',
    pattern: 'First name is not valid',
  },
  lastNameError: {
    required: 'Last name is required',
    pattern: 'Last name is not valid',
  },
  emailErrors: {
    required: 'Email is required',
    pattern: 'Email is not valid',
    emailRepeat: 'You have an account already registered',
    emailNotFound: 'Email does not exist as an active BTS account',
  },
  passwordErrors: {
    required: 'Password is required',
    pattern: '8 digits, at least 1 lower case, 1 upper case, 1 number, and one of these signs: @.-_!?',
  },
  validatePasswordErrors: {
    required: 'Password confirmation is required',
    pattern: 'Password confirmation is not valid',
  },
  fieldErrors: {
    required: 'Field is required',
  },
  seniorityErrors: {
    required: 'Seniority is required',
  },
  positionErrors: {
    required: 'Position is required',
    pattern: 'Position is not valid',
  },
  locationErrors: {
    required: 'Location is required',
  },
  descriptionError: {
    required: 'Description is required',
    pattern: 'Description is not valid',
  },
};

export const setNewPasswordErrors = {
  passwordErrors: {
    required: 'Password is required',
    pattern: '8 digits, at least 1 minus, 1 mayus, 1 number, and one of these signs: @.-_!?',
  },
  confirmPasswordErrors: {
    required: 'Confirm password is required',
  },
};

export const loginErrors = {
  emailErrors: {
    required: 'Email is required',
    pattern: 'Email is not valid',
  },
  passwordErrors: {
    required: 'Password is required',
  },
};

export const forgotPasswordErrors = {
  emailErrors: {
    required: 'Email is required',
    pattern: 'Email is not valid',
  },
};
