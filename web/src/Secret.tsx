import {Link as RouterLink, useParams} from "react-router-dom";
import React, {useEffect} from "react";
import {
  Alert,
  Box,
  Button,
  Grid,
  Link,
  Snackbar,
  Stack,
  TextField,
  Typography
} from "@mui/material";
import LoadingButton from '@mui/lab/LoadingButton';
import axios, {AxiosResponse} from "axios";
import AddBoxIcon from '@material-ui/icons/AddBox';
import { useNavigate } from 'react-router-dom'
import ReCAPTCHA from "react-google-recaptcha";
import CopyButton from "./CopyButton";
import SubTitle from "./SubTitle";


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
  "gettingSecret": false,
  "retrievedSecret": false,
  "saving": false,
  "errorSaving": false,
  "secretNotFound": false,
  "secretNotFoundId": '',
  "secretLink": '',
  "secretValue": ''
}

export default function Secret() {
  const [secretState, setSecretState] = React.useState(initialState);
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
      setSecretState(s => { return {
        ...s,
        ...{
         "gettingSecret": true
        }
      }});
      axios.get(`/api/secret/${params.secretId}`)
        .then((response: AxiosResponse<SecretEntry>) => {
          setSecretState(s => { return {
            ...s,
            ...{
              "viewingSecret": true,
              "gettingSecret": false,
              "secretEditable": false,
              "retrievedSecret": true,
              "secretValue": response.data.secret
            }
          }});
        })
        .catch((error) => {
          console.log(error)
          navigate("/", {replace: true});
          setSecretState(s => { return {
            ...s,
            ...{
              "secretNotFound": true,
              "gettingSecret": false,
              "secretNotFoundId": `${params.secretId}`
            }
          }});
        });
    }
  }, [params.secretId, secretState.retrievedSecret, navigate])
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
      <SubTitle text={secretState.viewingSecret ?
                        `Secret ${params.secretId} delivered` :
                        "Secret is saved for 24 hours. Secret is only viewable one time."}
                loading={secretState.gettingSecret}/>
      <TextField disabled={!secretState.secretEditable}
                 id="secret-text-field"
                 multiline
                 rows={8}
                 onChange={handleTextFieldChange}
                 value={secretState.secretValue}
      />
      {secretState.viewingSecret &&
      <CopyButton text={secretState.secretValue} innerText={"Copy"} disableTip={true}/>
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
      <Grid container style={{
        justifyContent: "center",
        alignItems: "center",
      }}>
          <Grid item>
            <CopyButton text={secretState.secretLink}/>
          </Grid>
          <Grid item>
            <Typography id="secret-url" variant={"h6"}>
                <Link href={secretState.secretLink}>
                  {secretState.secretLink}
                </Link>
            </Typography>
          </Grid>
      </Grid>
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