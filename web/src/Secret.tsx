import {Link as RouterLink, useParams} from "react-router-dom";
import React, {useEffect} from "react";
import {Alert, Box, Button, Link, Snackbar, Stack, TextField, Tooltip, Typography} from "@mui/material";
import LoadingButton from '@mui/lab/LoadingButton';
import axios, {AxiosResponse} from "axios";
import FileCopyIcon from '@material-ui/icons/FileCopy';
import AddBoxIcon from '@material-ui/icons/AddBox';
import { useNavigate } from 'react-router-dom'
import ReCAPTCHA from "react-google-recaptcha";


interface CreateSecretResponse {
  "secret": SecretEntry
  "url": string
}

interface SecretEntry {
  "id": string,
  "secret": string
}

const initialState = {
  "viewingSecret": false,
  "secretEditable": true,
  "retrievedSecret": false,
  "saving": false,
  "errorSaving": false,
  "secretNotFound": false,
  "secretNotFoundId": '',
  "secretLink": '',
  "secretValue": ''
}

export default function Secret() {
  const [secretState, setSecretState] = React.useState(initialState)
  let recaptchaRef: any;
  const setRecaptchaRef = (ref: any) => {
    if (ref) {
      return recaptchaRef = ref;
    }
  };
  const params = useParams();
  const navigate = useNavigate();
  useEffect(() => {
    if (params.secretId && params.secretId.length > 0 && !secretState.retrievedSecret) {
      axios.get(`/api/secret/${params.secretId}`)
        .then((response: AxiosResponse<SecretEntry>) => {
          setSecretState({
            ...secretState,
            ...{
              "viewingSecret": true,
              "secretEditable": false,
              "retrievedSecret": true,
              "secretValue": response.data.secret
            }
          });
        })
        .catch((error) => {
          console.log(error)
          navigate("/", {replace: true});
          setSecretState({
            ...secretState,
            ...{
              "secretNotFound": true,
              "secretNotFoundId": `${params.secretId}`
            }
          });
        });
    }
  })
  const handleTextFieldChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSecretState({
      ...secretState,
      ...{
        "secretValue": event.target.value
      }
    });
  };
  const handleSubmit = (value: string | null) => {
    setSecretState({
      ...secretState,
      ...{
        "secretEditable": false,
        "saving": true
      }
    });
    axios.post(`/api/secret?recaptcha=${value}`, {secret: secretState.secretValue})
      .then((response: AxiosResponse<CreateSecretResponse>) => {
        recaptchaRef.reset();
        setSecretState({
          ...secretState,
          ...{
            "secretEditable": false,
            "saving": false,
            "errorSaving": false,
            "secretLink": `${window.location.origin}/${response.data.secret.id}`
          }
        });
      })
      .catch((error) => {
        recaptchaRef.reset();
        setSecretState({
          ...secretState,
          ...{
            "secretEditable": true,
            "saving": false,
            "errorSaving": true
          }
        });
        console.log(error)
      });
  };
  return (
    <Stack style={{ maxWidth: "1000px", width: "100%" }} direction={"column"} spacing={2} m={2}>
      <Typography variant={"h3"}>
        Secrets
      </Typography>
      {secretState.viewingSecret &&
      <Typography variant={"subtitle1"}>
        {`Secret ${params.secretId} delivered`}
      </Typography>
      }
      {!secretState.viewingSecret &&
      <Typography variant={"subtitle1"}>
          Secret is saved for 24 hours. Secret is only viewable one time.
      </Typography>
      }
      <TextField disabled={!secretState.secretEditable}
                 id="secret-text-field"
                 multiline
                 rows={8}
                 onChange={handleTextFieldChange}
                 value={secretState.secretValue}
      />
      {secretState.viewingSecret &&
      <Button id="copy-button" onClick={() => {navigator.clipboard.writeText(secretState.secretValue)}}>
        <FileCopyIcon/>
        <Box ml={1}>Copy</Box>
      </Button>
      }
      {(secretState.viewingSecret || (secretState.secretLink !== null && secretState.secretLink.length > 0)) &&
      <Button id="new-button" onClick={() => setSecretState(initialState)} component={RouterLink} to={"/"}>
          <AddBoxIcon/>
          <Box ml={1}>New</Box>
      </Button>
      }
      {!secretState.viewingSecret && secretState.secretLink !== null && secretState.secretLink.length === 0 &&
      <LoadingButton id="submit-button" loading={secretState.saving} onClick={() => recaptchaRef.execute()}>Submit</LoadingButton>
      }
      <Snackbar anchorOrigin={{vertical: 'top', horizontal: 'center'}}
                open={secretState.errorSaving}
                autoHideDuration={15000}
                onClose={() => setSecretState({
                  ...secretState,
                  "errorSaving": false
                })}>
        <Alert id="save-error-alert" severity="error">Error saving secret</Alert>
      </Snackbar>
      <Snackbar anchorOrigin={{vertical: 'top', horizontal: 'center'}}
                open={secretState.secretNotFound}
                autoHideDuration={15000}
                onClose={() => setSecretState({
                  ...secretState,
                  "secretNotFound": false
                })}>
        <Alert id="not-found-alert" severity="error">{`${secretState.secretNotFoundId} not found`}</Alert>
      </Snackbar>
      {!secretState.viewingSecret && secretState.secretLink !== null && secretState.secretLink.length > 0 &&
      <Typography id="secret-url" variant={"h6"}>
          <Tooltip title={"Copy"}>
            <Button id="copy-link-button" onClick={() => {navigator.clipboard.writeText(secretState.secretLink)}}>
              <FileCopyIcon/>
            </Button>
          </Tooltip>
          <Link href={secretState.secretLink}>
            {secretState.secretLink}
          </Link>
      </Typography>
      }
      <ReCAPTCHA
        theme={"dark"}
        ref={(r) => setRecaptchaRef(r)}
        size="invisible"
        sitekey={`${process.env.REACT_APP_RECAPTCHA}`}
        onChange={handleSubmit}
      />
    </Stack>
  );
}